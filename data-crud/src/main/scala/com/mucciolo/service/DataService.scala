package com.mucciolo.service

import cats.effect.IO
import com.mucciolo.entity.Data
import com.mucciolo.repository.DataRepository
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.{HttpRoutes, MediaType}

class DataService(repository: DataRepository) extends Http4sDsl[IO] {

  private val DataPath: Path = Root / "data"

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> DataPath =>
      Ok(
        Stream("[") ++ repository.getAll.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"),
        `Content-Type`(MediaType.application.json)
      )

    case GET -> DataPath / LongVar(id) =>
      for {
        result <- repository.findById(id)
        response <- result match {
          case Some(data) => Ok(data.asJson)
          case None => NotFound()
        }
      } yield response

    case req @ POST -> DataPath =>
      for {
        inputData <- req.decodeJson[Data]
        createdData <- repository.insert(inputData.value)
        response <- Created(createdData.asJson)
      } yield response

    case req @ PUT -> DataPath / LongVar(id) =>
      for {
        data <- req.decodeJson[Data]
        result <- repository.update(id, data)
        response <- result match {
          case Some(data) => Ok(data.asJson)
          case None => NotFound()
        }
      } yield response

    case DELETE -> DataPath / LongVar(id) =>
      repository.delete(id).flatMap {
        case Some(_) => NoContent()
        case None => NotFound()
      }

  }

}
