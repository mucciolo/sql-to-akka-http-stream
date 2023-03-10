package com.mucciolo.http

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.{Directives, Route}
import com.mucciolo.config.HttpServerConf
import spray.json.DefaultJsonProtocol

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

object HttpServer extends Directives with SprayJsonSupport with DefaultJsonProtocol {

  def runForever(
    config: HttpServerConf, routes: Route
  )(implicit system: ActorSystem[_]): Future[Http.ServerBinding] = {

    implicit val ec: ExecutionContext = system.executionContext

    Http().newServerAt(config.host, config.port)
      .bind(routes)
      .map(_.addToCoordinatedShutdown(5.seconds))
  }
}
