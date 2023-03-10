package com.mucciolo.http

import cats.effect.{IO, Resource}
import com.comcast.ip4s.{Host, IpLiteralSyntax, Port}
import com.mucciolo.config.{AppConf, ServerConf}
import com.mucciolo.database.Database
import com.mucciolo.repository.PostgresDataRepository
import com.mucciolo.routes.DataRoutes
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.server.middleware.Logger
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

object HttpServer {

  private object Default {
    val Host = ipv4"127.0.0.1"
    val Port = port"8080"
  }

  def runForever(): IO[Nothing] = {
    for {
      config <- Resource.eval(ConfigSource.default.loadF[IO, AppConf]())
      ec <- ExecutionContexts.cachedThreadPool[IO]
      transactor <- Database.newTransactorResource(config.database, ec)
      _ <- Database.newMigrationResource(transactor)
      repository = new PostgresDataRepository(transactor)
      routes = DataRoutes(repository)
      _ <- buildEmberServer(config.server, routes)
    } yield ()
  }.useForever

  private def buildEmberServer(config: ServerConf, routes: HttpRoutes[IO]): Resource[IO, Server] = {
    EmberServerBuilder.default[IO]
      .withHost(Host.fromString(config.host).getOrElse(Default.Host))
      .withPort(Port.fromInt(config.port).getOrElse(Default.Port))
      .withHttpApp(
        Logger.httpApp(logHeaders = config.logHeaders, logBody = config.logBody)(routes.orNotFound)
      )
      .build
  }
}
