package com.mucciolo

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.mucciolo.config.AppConf
import com.mucciolo.influx.InfluxClient
import com.mucciolo.http.HttpServer
import com.mucciolo.routes.InfluxRoutes
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import com.mucciolo.util.Log

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object InfluxStreamApp extends App with Log {

  ConfigSource.default.load[AppConf] match {

    case Left(failures) =>
      log.error(failures.prettyPrint())

    case Right(config) =>

      implicit val system: ActorSystem[_] = ActorSystem[Nothing](Behaviors.empty, "influx-stream")
      implicit val ec: ExecutionContext = system.executionContext

      val repository = InfluxClient(config.influx)
      val routes = InfluxRoutes(repository)

      HttpServer.runForever(config.server, routes)
        .onComplete {
          case Success(binding) =>
            log.info("Server started on http://{}:{}",
              binding.localAddress.getHostName, binding.localAddress.getPort)

          case Failure(exception) =>
            log.error(exception.getMessage)
            system.terminate()
        }
  }

}
