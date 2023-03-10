package com.mucciolo

import akka.Done
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.mucciolo.config._
import com.mucciolo.core.InfluxDbSinkConnector
import com.mucciolo.util.Log
import pureconfig._
import pureconfig.generic.auto._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

object InfluxDbSinkConnectorApp extends App with Log {

  private val consumer: Future[Done] = ConfigSource.default.load[AppConf] match {

    case Left(failures) =>
      log.error(failures.prettyPrint())
      Future.successful(Done)

    case Right(config) =>
      implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, config.connector.name)
      InfluxDbSinkConnector.runForever(config)

  }

  Await.ready(consumer, Duration.Inf)

}
