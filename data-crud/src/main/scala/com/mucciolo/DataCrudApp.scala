package com.mucciolo

import cats.effect.{IO, IOApp}
import com.mucciolo.server.HttpServer

object DataCrudApp extends IOApp.Simple {
  def run: IO[Unit] = HttpServer.run()
}
