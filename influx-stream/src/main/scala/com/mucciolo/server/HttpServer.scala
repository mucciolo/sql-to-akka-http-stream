package com.mucciolo.server

import akka.actor.typed.ActorSystem
import akka.event.Logging
import akka.event.Logging.LogLevel
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import com.mucciolo.config.HttpServerConf
import com.mucciolo.influx.{InfluxRepository, TaggedValue}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.Future

object HttpServer extends Directives with SprayJsonSupport with DefaultJsonProtocol {

  private implicit val taggedValueJsonFormat: RootJsonFormat[TaggedValue] = jsonFormat2(TaggedValue)
  private val newLine = ByteString("\n")
  private implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
    EntityStreamingSupport.json().withFramingRenderer(Flow[ByteString].map(_ ++ newLine))

  def run(config: HttpServerConf, influxRepository: InfluxRepository)
         (implicit actorSystem: ActorSystem[_]): Future[Http.ServerBinding] = {

    val routes = buildRoutes(influxRepository)
    val routesLogged = DebuggingDirectives.logRequestResult("influx-stream", Logging.DebugLevel)(routes)

    Http().newServerAt(config.host, config.port).bind(routesLogged)
  }

  private def buildRoutes(influxRepository: InfluxRepository) = {

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
