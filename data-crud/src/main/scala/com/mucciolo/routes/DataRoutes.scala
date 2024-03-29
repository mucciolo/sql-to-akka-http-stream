package com.mucciolo.routes

import cats.effect.IO
import com.mucciolo.domain.Data
import com.mucciolo.repository.DataRepository
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.{HttpRoutes, MediaType}

object DataRoutes extends Http4sDsl[IO] {

  private val DataPath: Path = Root / "data"

  private object QueryParam {
    object Limit extends QueryParamDecoderMatcher[Int]("limit")
    object Offset extends QueryParamDecoderMatcher[Int]("offset")
    object MinValue extends OptionalQueryParamDecoderMatcher[Int]("min")
  }

  def apply(repository: DataRepository): HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> DataPath :? QueryParam.Limit(limit) +& QueryParam.Offset(offset) +& QueryParam.MinValue(min)  =>
      Ok(
        Stream("[") ++ repository.get(limit, offset, min).map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"),
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
        data <- req.decodeJson[Data]
        createdData <- repository.insert(data)
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
