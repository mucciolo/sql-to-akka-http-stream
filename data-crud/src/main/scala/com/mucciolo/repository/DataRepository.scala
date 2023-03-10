package com.mucciolo.repository

import cats.effect.IO
import com.mucciolo.domain.Data

trait DataRepository {
  def insert(data: Data): IO[Data]
  def get(limit: Int, offset: Int, min: Option[Int] = None): fs2.Stream[IO, Data]
  def findById(id: Long): IO[Option[Data]]
  def update(id: Long, data: Data): IO[Option[Data]]
  def delete(id: Long): IO[Option[Unit]]
}
