package com.mucciolo.routes

import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import com.mucciolo.influx.{InfluxClient, TaggedValue}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object InfluxRoutes extends Directives with SprayJsonSupport with DefaultJsonProtocol {

  private implicit val taggedValueJsonFormat: RootJsonFormat[TaggedValue] = jsonFormat2(TaggedValue)
  private val newLine = ByteString("\n")
  private implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
    EntityStreamingSupport.json().withFramingRenderer(Flow[ByteString].map(_ ++ newLine))

  def apply(influxRepository: InfluxClient): Route = {
    path("moving-average" / LongNumber) { id =>
      get {
        parameters("period".as[Long], "every".as[Int].withDefault(1)) { (period, every) => {
          complete(influxRepository.movingAverage(id, period, every))
        }
        }
      }
    }
  }
}
