package com.mucciolo.core

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.Supervision
import akka.stream.scaladsl.{Flow, Keep}
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.mucciolo.config._
import com.mucciolo.util.Log
import io.apicurio.registry.rest.client.RegistryClientFactory
import io.apicurio.registry.serde.avro.AvroKafkaDeserializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.Deserializer

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.MapHasAsJava
import scala.language.postfixOps

object InfluxDbSinkConnector extends Log {

  private object Measurement {
    val id = "id"
    val field = "value"
  }

  def runForever(config: AppConf)(implicit system: ActorSystem[_]): Future[Done] = {

    implicit val ec: ExecutionContext = system.executionContext

    val influxDb = InfluxDBClientFactory.create(config.influx.asInfluxDBClientOptions).getWriteApiBlocking
    val kafkaConsumerSettings = buildKafkaConsumerSettings(config.kafka)
    val convertGenericRecordToPoint = genericRecordToPoint(config.connector)(_)

    val (control, consumer) = Consumer
      .sourceWithOffsetContext(kafkaConsumerSettings, Subscriptions.topics(config.kafka.topic))
      .via(Flow.fromFunction {
        case (record, offset) => (convertGenericRecordToPoint(record), offset)
      })
      .via(Flow.fromFunction {
        case (point, offset) => (influxDb.writePoint(point), offset)
      })
      .withAttributes(supervisionStrategy(Supervision.resumingDecider))
      .toMat(Committer.sinkWithOffsetContext(CommitterSettings(system)))(Keep.both)
      .run()

    CoordinatedShutdown(system).addJvmShutdownHook(() => {
      control.drainAndShutdown(consumer)
    })

    consumer
  }

  private def buildKafkaConsumerSettings(kafkaConf: KafkaConf)(implicit system: ActorSystem[_]) = {

    val registryClient = RegistryClientFactory.create(kafkaConf.schemaRegistryUrl)
    val kafkaAvroDeserializer = new AvroKafkaDeserializer(registryClient)
    kafkaAvroDeserializer.configure(Map.empty[String, Any].asJava, false)
    val genericRecordDeserializer = kafkaAvroDeserializer.asInstanceOf[Deserializer[GenericRecord]]

    ConsumerSettings(system, genericRecordDeserializer, genericRecordDeserializer)
      .withBootstrapServers(kafkaConf.kafkaBootstrapServers)
      .withGroupId(kafkaConf.groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withStopTimeout(0 seconds)

  }

  private def genericRecordToPoint(config: ConnectorConf)(record: ConsumerRecord[_, GenericRecord]): Point = {

    log.debug("{}", record)

    Point.measurement(config.measurementName)
      .addTags(Map(Measurement.id -> record.value.get(config.id).toString).asJava)
      .addField(Measurement.field, record.value.get(config.field).toString.toLong)
      .time(record.value.get(config.time).toString.toLong, WritePrecision.MS)
  }

}
