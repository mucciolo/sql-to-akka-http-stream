import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.mucciolo.domain._
import com.mucciolo.repository.DataRepository
import com.mucciolo.routes.DataRoutes
import io.circe.syntax._
import fs2._
import org.http4s.Method.{GET, POST, PUT}
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.dsl.io.DELETE
import org.http4s.implicits._
import org.http4s.{Request, Response, Status, Uri}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

final class DataRoutesSpec extends AsyncWordSpec with AsyncIOSpec with AsyncMockFactory with Matchers {

  private val repository = mock[DataRepository]
  private val routes = DataRoutes(repository)

  private def send(request: Request[IO]): IO[Response[IO]] = {
    routes.orNotFound(request)
  }

  "DataRoutes" when {
    "POST /data" should {
      "return 201 given valid input" in {

        val requestData = Data(None, 2)
        val expectedCreatedData = requestData.copy(id = Some(1))

        (repository.insert _).expects(requestData).returns(IO.pure(expectedCreatedData))

        val requestJson = requestData.asJson.toString()
        val request = Request[IO](POST, uri"/data").withEntity(requestJson)
        for {
          res <- send(request)
          status = res.status
          body <- res.as[Data]
        } yield {
          status shouldBe Status.Created
          body shouldBe expectedCreatedData
        }

      }
    }

    "PUT /data/:id" should {
      "return 200 given existing id" in {

        val id = 1
        val requestData = Data(None, 2)
        val expectedCreatedData = requestData.copy(id = Some(id))

        (repository.update _).expects(id, requestData).returns(IO.pure(Some(expectedCreatedData)))

        val requestJson = requestData.asJson.toString()
        val request = Request[IO](PUT, Uri.unsafeFromString(s"/data/$id")).withEntity(requestJson)

        for {
          res <- send(request)
          status = res.status
          body <- res.as[Data]
        } yield {
          status shouldBe Status.Ok
          body shouldBe expectedCreatedData
        }

      }

      "return 404 given id does not exist" in {

        val id = 1
        val requestData = Data(None, 2)

        (repository.update _).expects(id, requestData).returns(IO.pure(None))

        val requestJson = requestData.asJson.toString()
        val request = Request[IO](PUT, Uri.unsafeFromString(s"/data/$id")).withEntity(requestJson)
        val response = send(request)

        response.asserting(_.status shouldBe Status.NotFound)

      }
    }

    "GET /data/:id" should {
      "return 200 given existing id" in {

        val id = 1
        val requestData = Data(None, 2)
        val expectedCreatedData = requestData.copy(id = Some(id))

        (repository.findById _).expects(id).returns(IO.pure(Some(expectedCreatedData)))

        val requestJson = requestData.asJson.toString()
        val request = Request[IO](GET, Uri.unsafeFromString(s"/data/$id")).withEntity(requestJson)

        for {
          res <- send(request)
          status = res.status
          body <- res.as[Data]
        } yield {
          status shouldBe Status.Ok
          body shouldBe expectedCreatedData
        }

      }

      "return 204 given that id does not exist" in {

        val id = 1
        val requestData = Data(None, 2)

        (repository.findById _).expects(id).returns(IO.pure(None))

        val requestJson = requestData.asJson.toString()
        val request = Request[IO](GET, Uri.unsafeFromString(s"/data/$id")).withEntity(requestJson)
        val response = send(request)

        response.asserting(_.status shouldBe Status.NotFound)

      }
    }

    "GET /data" should {
      "return 200" in {

        val dataStream = Stream(
          Data(Some(1), 11),
          Data(Some(2), 22),
          Data(Some(3), 33)
        )
        val limit = 10
        val offset = 0

        (repository.get _).expects(limit, offset, None).returns(dataStream)

        val request = Request[IO](GET, Uri.unsafeFromString(s"/data?limit=$limit&offset=$offset"))

        for {
          res <- send(request)
          status = res.status
          body <- res.as[List[Data]]
        } yield {
          status shouldBe Status.Ok
          body shouldBe dataStream.toList
        }

      }
    }

    "DELETE /data/:id" should {
      "return 204 given existing id" in {

        val id = 1
        (repository.delete _).expects(id).returns(IO.pure(Some(())))

        val request = Request[IO](DELETE, Uri.unsafeFromString(s"/data/$id"))
        val response = send(request)

        response.asserting(_.status shouldBe Status.NoContent)

      }

      "return 404 given that id does not exist" in {

        val id = 1
        (repository.delete _).expects(id).returns(IO.pure(None))

        val request = Request[IO](DELETE, Uri.unsafeFromString(s"/data/$id"))
        val response = send(request)

        response.asserting(_.status shouldBe Status.NotFound)

      }
    }
  }

}
