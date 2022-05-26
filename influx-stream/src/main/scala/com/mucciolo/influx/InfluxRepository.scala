package com.mucciolo.influx

import akka.actor.Cancellable
import akka.event.Logging
import akka.stream.Attributes
import akka.stream.scaladsl.Source
import com.influxdb.client.scala.InfluxDBClientScalaFactory
import com.influxdb.query.dsl.Flux
import com.influxdb.query.dsl.functions.restriction.Restrictions.{and, field, measurement, tag}
import com.mucciolo.config.InfluxDbConf
import org.slf4j.{Logger, LoggerFactory}

import java.time.temporal.ChronoUnit
import scala.concurrent.duration.{DurationInt, DurationLong}

final case class InfluxRepository(config: InfluxDbConf) {

  private val log: Logger = LoggerFactory.getLogger(classOf[InfluxRepository])

  private val client = InfluxDBClientScalaFactory.create(config.asInfluxDBClientOptions).getQueryScalaApi()

  def movingAverage(id: Long, period: Long, every: Long): Source[TaggedValue, Cancellable] = {

    val query = Flux.from(config.bucket)
      .range(-period, ChronoUnit.SECONDS)
      .filter(and(measurement().equal("postgres.data"), field().equal("value"), tag("id").equal(id.toString)))
      .mean()
      .toString

    log.debug(s"moving-average {id = $id, period = $period, every = $every}")
    log.debug(query)

    Source.tick(0.seconds, every.seconds, query)
      .flatMapConcat(query =>
        client.query(query)
          .map(record => TaggedValue(record.getStop.toEpochMilli, record.getValue.asInstanceOf[Double]))
          .log(s"moving-average {id = $id, period = $period}")
          .addAttributes(Attributes.logLevels(Attributes.logLevelDebug))
      )

  }

}
