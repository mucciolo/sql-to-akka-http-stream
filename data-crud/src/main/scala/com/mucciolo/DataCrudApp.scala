package com.mucciolo

import cats.effect.{IO, IOApp}
import com.mucciolo.http.HttpServer

object DataCrudApp extends IOApp.Simple {
  def run: IO[Unit] = HttpServer.runForever()
}
