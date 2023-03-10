package com.mucciolo.database

import cats.effect._
import com.mucciolo.config.DatabaseConf
import com.zaxxer.hikari.HikariDataSource
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult

import scala.concurrent.ExecutionContext

object Database {

  def newTransactorResource(
    conf: DatabaseConf, ec: ExecutionContext
  ): Resource[IO, HikariTransactor[IO]] = {
    HikariTransactor.newHikariTransactor[IO]( conf.driver, conf.url, conf.user, conf.pass, ec)
  }

  def newMigrationResource(transactor: HikariTransactor[IO]): Resource[IO, MigrateResult] = {
    Resource.eval(transactor.configure(dataSource => IO(migrate(dataSource))))
  }

  private def migrate(dataSource: HikariDataSource): MigrateResult = {
    Flyway.configure().dataSource(dataSource).load().migrate()
  }

}
