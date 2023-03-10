package com.mucciolo

import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.mucciolo.config.AppConf
import com.mucciolo.influx.InfluxRepository
import com.mucciolo.server.HttpServer
import org.slf4j.{Logger, LoggerFactory}
import pureconfig.ConfigSource
import pureconfig._
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object InfluxStreamApp extends App {

  private val log: Logger = LoggerFactory.getLogger(getClass)

  ConfigSource.default.load[AppConf] match {

    case Left(failures) =>
      log.error(failures.prettyPrint())

    case Right(config) =>
      implicit val actorSystem: ActorSystem[_] = ActorSystem[_](Behaviors.empty, "influx-stream")
      implicit val executionContext: ExecutionContextExecutor = actorSystem.executionContext

      HttpServer.run(config.server, InfluxRepository(config.influx)).onComplete {

        case Success(binding) =>
          log.info("Server started on http://{}:{}", binding.localAddress.getHostName, binding.localAddress.getPort)
          CoordinatedShutdown(actorSystem).addJvmShutdownHook(() => {
            binding.terminate(10.seconds)
          })

        case Failure(exception) =>
          log.error(exception.getMessage)
          actorSystem.terminate()
      }

  }

}
