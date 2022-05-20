package com.mucciolo

import akka.Done
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.mucciolo.connector.InfluxDbSinkConnector
import com.mucciolo.connector.config.AppConf
import org.slf4j.{Logger, LoggerFactory}
import pureconfig._
import pureconfig.generic.auto._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

object InfluxDbSinkConnectorApp extends App {

  private val log: Logger = LoggerFactory.getLogger(getClass)

  private val stream: Future[Done] = ConfigSource.default.load[AppConf] match {

    case Left(failures) =>
      log.error(failures.prettyPrint())
      Future.successful(Done)

    case Right(config) =>
      implicit val actorSystem: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, config.connector.name)
      InfluxDbSinkConnector.run(config)

  }

  Await.ready(stream, Duration.Inf)

}
