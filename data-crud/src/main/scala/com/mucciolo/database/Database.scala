package com.mucciolo.database

import cats.effect.{Resource, _}
import com.typesafe.config.Config
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext

object Database {

  object ConfigKey {
    val Driver: String = "database.driver"
    val Url: String = "database.url"
    val User: String = "database.user"
    val Pass: String = "database.pass"
  }

  def transactor(config: Config, executionContext: ExecutionContext): Resource[IO, HikariTransactor[IO]] = {
    HikariTransactor.newHikariTransactor[IO](
      config.getString(ConfigKey.Driver),
      config.getString(ConfigKey.Url),
      config.getString(ConfigKey.User),
      config.getString(ConfigKey.Pass),
      executionContext
    )
  }

  def migrate(transactor: HikariTransactor[IO]): Resource[IO, Unit] = {
    Resource.eval {
      transactor.configure { dataSource =>
        IO {
          Flyway.configure().dataSource(dataSource).load().migrate()
          ()
        }
      }
    }
  }

}
