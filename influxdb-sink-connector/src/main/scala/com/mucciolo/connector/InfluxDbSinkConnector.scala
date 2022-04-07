package com.mucciolo.connector

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import akka.stream.ActorAttributes.{SupervisionStrategy, supervisionStrategy}
import akka.stream.Supervision
import akka.stream.scaladsl.{Flow, Keep}
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.mucciolo.connector.config.{InfluxDbConf, KafkaConf}
import io.apicurio.registry.rest.client.RegistryClientFactory
import io.apicurio.registry.serde.avro.AvroKafkaDeserializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.MapHasAsJava
import scala.language.postfixOps

object InfluxDbSinkConnector {

  private val log: Logger = LoggerFactory.getLogger(getClass)

  def run(influxDbConf: InfluxDbConf, kafkaConf: KafkaConf)(implicit actorSystem: ActorSystem[Nothing]): Future[Done] = {

    implicit val executionContext: ExecutionContext = actorSystem.executionContext

    val influxDb = InfluxDBClientFactory.create(influxDbConf.asInfluxDBClientOptions).getWriteApiBlocking
    val kafkaConsumerSettings = buildKafkaConsumerSettings(kafkaConf)
    val (control, stream) = Consumer
      .sourceWithOffsetContext(kafkaConsumerSettings, Subscriptions.topics(kafkaConf.topic))
      .via(Flow.fromFunction {
        case (record, offset) => (genericRecordToPoint(record), offset)
      })
      .via(Flow.fromFunction {
        case (point, offset) => (influxDb.writePoint(point), offset)
      })
      .withAttributes(supervisionStrategy(Supervision.resumingDecider))
      .toMat(Committer.sinkWithOffsetContext(CommitterSettings(actorSystem)))(Keep.both)
      .run()

    CoordinatedShutdown(actorSystem).addJvmShutdownHook(() => {
      control.drainAndShutdown(stream)
    })

    stream
  }

  private def buildKafkaConsumerSettings(kafkaConf: KafkaConf)(implicit actorSystem: ActorSystem[Nothing]) = {

    val registryClient = RegistryClientFactory.create(kafkaConf.schemaRegistryUrl)
    val kafkaAvroDeserializer = new AvroKafkaDeserializer(registryClient)
    kafkaAvroDeserializer.configure(Map.empty[String, Any].asJava, false)
    val genericRecordDeserializer = kafkaAvroDeserializer.asInstanceOf[Deserializer[GenericRecord]]

    ConsumerSettings(actorSystem, genericRecordDeserializer, genericRecordDeserializer)
      .withBootstrapServers(kafkaConf.kafkaBootstrapServers)
      .withGroupId(kafkaConf.groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withStopTimeout(0 seconds)

  }

  private def genericRecordToPoint(record: ConsumerRecord[_, GenericRecord]): Point = {

    log.debug("{}", record)

    Point.measurement("postgres.data")
      .addTags(Map("id" -> record.value.get("id").toString).asJava)
      .addField("value", record.value.get("value").toString.toLong)
      .time(record.value.get("timestamp").toString.toLong, WritePrecision.MS)
  }

}
