package com.mucciolo.server

import akka.actor.typed.ActorSystem
import akka.event.Logging
import akka.event.Logging.LogLevel
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import com.mucciolo.config.HttpServerConf
import com.mucciolo.influx.{InfluxRepository, TaggedValue}
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future

object HttpServer extends Directives with SprayJsonSupport with DefaultJsonProtocol {

  private implicit val taggedValueJsonFormat = jsonFormat2(TaggedValue)
  private val newLine = ByteString("\n")
  private implicit val jsonStreamingSupport = EntityStreamingSupport.json()
    .withFramingRenderer(Flow[ByteString].map(bs => bs ++ newLine))

  def run(config: HttpServerConf, influxRepository: InfluxRepository)
         (implicit actorSystem: ActorSystem[_]): Future[Http.ServerBinding] = {

    val routes = buildRoutes(influxRepository)
    val routesLogged = DebuggingDirectives.logRequestResult("influx-stream", Logging.DebugLevel)(routes)

    Http().newServerAt(config.host, config.port)
      .bind(routesLogged)
  }

  private def buildRoutes(influxRepository: InfluxRepository)(implicit actorSystem: ActorSystem[_]) = {

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
