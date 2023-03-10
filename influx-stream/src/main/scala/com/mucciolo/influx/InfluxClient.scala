package com.mucciolo.influx

import akka.NotUsed
import akka.stream.scaladsl._
import akka.stream.{Attributes, RestartSettings}
import com.influxdb.client.scala.InfluxDBClientScalaFactory
import com.influxdb.query.dsl.Flux
import com.influxdb.query.dsl.functions.restriction.Restrictions.{and, field, measurement, tag}
import com.mucciolo.config.InfluxDbConf
import com.mucciolo.util.Log

import java.time.temporal.ChronoUnit
import scala.concurrent.duration.{DurationInt, DurationLong}

final case class InfluxClient(config: InfluxDbConf) extends Log {

  private val client =
    InfluxDBClientScalaFactory.create(config.asInfluxDBClientOptions).getQueryScalaApi()

  private val restartSettings = RestartSettings(
    minBackoff = 3.seconds,
    maxBackoff = 30.seconds,
    randomFactor = 0.2
  ).withMaxRestarts(20, 5.minutes)

  def movingAverage(id: Long, period: Long, every: Long): Source[TaggedValue, NotUsed] = {

    val query = Flux.from(config.bucket)
      .range(-period, ChronoUnit.SECONDS)
      .filter(and(measurement().equal("postgres.data"), field().equal("value"), tag("id").equal(id.toString)))
      .mean()
      .toString

    log.debug(s"moving-average {id = $id, period = $period, every = $every}")
    log.debug(query)

    val movingAverageInfluxSource =
      Source.tick(0.seconds, every.seconds, query)
        .flatMapConcat(query =>
          client.query(query)
            .map(record => TaggedValue(record.getStop.toEpochMilli, record.getValue.asInstanceOf[Double]))
            .log(s"moving-average {id = $id, period = $period}")
            .addAttributes(Attributes.logLevels(Attributes.logLevelDebug))
        )

    RestartSource.onFailuresWithBackoff(restartSettings)(() => movingAverageInfluxSource)
  }

}
