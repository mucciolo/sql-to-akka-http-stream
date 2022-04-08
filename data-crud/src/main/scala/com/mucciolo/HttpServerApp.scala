package com.mucciolo

import cats.effect.{IO, IOApp}
import com.mucciolo.server.HttpServer

object HttpServerApp extends IOApp.Simple {
  def run: IO[Unit] = HttpServer.run()
}
