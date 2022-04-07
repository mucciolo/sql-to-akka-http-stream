package com.mucciolo.server

import cats.effect.IO
import com.comcast.ip4s._
import com.mucciolo.database.Database
import com.mucciolo.repository.DataRepository
import com.mucciolo.service.DataService
import com.typesafe.config.ConfigFactory
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext

object HttpServer {

  object ConfigKey {
    val Host: String = "server.host"
    val Port: String = "server.port"
    val LogHeaders: String = "server.logHeaders"
    val LogBody: String = "server.logBody"
  }

  def run(configResourceBaseName: String = "application.conf"): IO[Nothing] = {

    val config = ConfigFactory.load(configResourceBaseName)

    for {
      transactor <- Database.transactor(config, ExecutionContext.global)
      _ <- Database.migrate(transactor)
      _ <- EmberServerBuilder.default[IO]
        .withHost(Host.fromString(config.getString(ConfigKey.Host)).getOrElse(ipv4"127.0.0.1"))
        .withPort(Port.fromInt(config.getInt(ConfigKey.Port)).getOrElse(port"8080"))
        .withHttpApp(Logger.httpApp(
          logHeaders = config.getBoolean(ConfigKey.LogHeaders),
          logBody = config.getBoolean(ConfigKey.LogBody)
        )(new DataService(new DataRepository(transactor)).routes.orNotFound))
        .build
    } yield ()
  }.useForever

}
