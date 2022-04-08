package com.mucciolo.server

import cats.effect.{IO, Resource}
import com.comcast.ip4s.{Host, IpLiteralSyntax, Port}
import com.mucciolo.config.{AppConf, ServerConf}
import com.mucciolo.database.Database
import com.mucciolo.repository.DataRepository
import com.mucciolo.service.DataService
import doobie.hikari.HikariTransactor
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.server.middleware.Logger
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

import scala.concurrent.ExecutionContext

object HttpServer {

  object Default {
    val host = ipv4"127.0.0.1"
    val port = port"8080"
  }

  def run(): IO[Nothing] = {
    for {
      config <- Resource.eval(ConfigSource.default.loadF[IO, AppConf]())
      transactor <- Database.transactor(config.database, ExecutionContext.global)
      _ <- Database.migrate(transactor)
      _ <- buildEmberServer(config.server, transactor)
    } yield ()
  }.useForever

  private def buildEmberServer(config: ServerConf, transactor: HikariTransactor[IO]): Resource[IO, Server] = {
    EmberServerBuilder.default[IO]
      .withHost(Host.fromString(config.host).getOrElse(Default.host))
      .withPort(Port.fromInt(config.port).getOrElse(Default.port))
      .withHttpApp(
        Logger.httpApp(
          logHeaders = config.logHeaders,
          logBody = config.logBody
        )(new DataService(new DataRepository(transactor)).routes.orNotFound)
      )
      .build
  }
}
