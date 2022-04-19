package com.mucciolo.repository

import cats.effect.IO
import com.mucciolo.entity.Data
import doobie.util.transactor.Transactor
import fs2.Stream
import doobie.implicits._

class DataRepository(transactor: Transactor[IO]) {

  def insert(data: Data): IO[Data] = {
    sql"INSERT INTO data(value) VALUES (${data.value})".update
      .withUniqueGeneratedKeys[Data]("id", "value")
      .transact(transactor)
  }

  def getAll: Stream[IO, Data] = {
    sql"SELECT * FROM data".query[Data].stream.transact(transactor)
  }

  def findById(id: Long): IO[Option[Data]] = {
    sql"SELECT * FROM data WHERE id = $id".query[Data].option.transact(transactor)
  }

  def update(id: Long, data: Data): IO[Option[Data]] = {
    sql"UPDATE data SET value = ${data.value} WHERE id = $id".update
      .run
      .transact(transactor)
      .map { affectedRowsCount => if (affectedRowsCount == 1) Some(data.copy(id = Option(id))) else None }
  }

  def delete(id: Long): IO[Option[Unit]] = {
    sql"DELETE FROM data WHERE id = $id".update
      .run
      .transact(transactor)
      .map { affectedRowsCount => if (affectedRowsCount == 1) Some(()) else None }
  }

}
