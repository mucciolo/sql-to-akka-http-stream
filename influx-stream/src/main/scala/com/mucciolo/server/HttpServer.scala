package com.mucciolo.server

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
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
    Http().newServerAt(config.host, config.port).bind(buildRoute(influxRepository))
  }

  private def buildRoute(influxRepository: InfluxRepository)(implicit actorSystem: ActorSystem[_]) = {

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
