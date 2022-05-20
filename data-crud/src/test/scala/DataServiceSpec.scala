import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.mucciolo.entity.Data
import com.mucciolo.repository.DataRepository
import com.mucciolo.service.DataService
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import fs2._
import org.http4s.Method.{GET, POST, PUT}
import org.http4s.circe.jsonDecoder
import org.http4s.dsl.io.DELETE
import org.http4s.implicits._
import org.http4s.{Request, Response, Status, Uri}
import org.scalamock.scalatest.MockFactory
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class DataServiceSpec extends AnyWordSpec with MockFactory with Matchers {

  private val repository = stub[DataRepository]
  private val service = new DataService(repository).routes

  private def send(request: Request[IO]): Response[IO] = {
    service.orNotFound(request).unsafeRunSync()
  }

  "DataService" should {

    "insert data" in {

      val requestData = Data(None, 2)
      val expectedCreatedData = requestData.copy(id = Some(1))

      (repository.insert _).when(requestData).returns(IO.pure(expectedCreatedData))

      val requestJson = requestData.asJson.toString()
      val request = Request[IO](POST, uri"/data").withEntity(requestJson)
      val response = send(request)

      response.status shouldBe Status.Created
      response.as[Json].unsafeRunSync() shouldBe expectedCreatedData.asJson

    }

    "update data when id exists" in {

      val id = 1
      val requestData = Data(None, 2)
      val expectedCreatedData = requestData.copy(id = Some(id))

      (repository.update _).when(id, requestData).returns(IO.pure(Some(expectedCreatedData)))

      val requestJson = requestData.asJson.toString()
      val request = Request[IO](PUT, Uri.unsafeFromString(s"/data/$id")).withEntity(requestJson)
      val response = send(request)

      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe expectedCreatedData.asJson

    }

    "do not update data when id does not exist" in {

      val id = 1
      val requestData = Data(None, 2)

      (repository.update _).when(id, requestData).returns(IO.pure(None))

      val requestJson = requestData.asJson.toString()
      val request = Request[IO](PUT, Uri.unsafeFromString(s"/data/$id")).withEntity(requestJson)
      val response = send(request)

      response.status shouldBe Status.NotFound

    }

    "return a single data when id exists" in {

      val id = 1
      val requestData = Data(None, 2)
      val expectedCreatedData = requestData.copy(id = Some(id))

      (repository.findById _).when(id).returns(IO.pure(Some(expectedCreatedData)))

      val requestJson = requestData.asJson.toString()
      val request = Request[IO](GET, Uri.unsafeFromString(s"/data/$id")).withEntity(requestJson)
      val response = send(request)

      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe expectedCreatedData.asJson

    }

    "do not return a single data when id does not exist" in {

      val id = 1
      val requestData = Data(None, 2)

      (repository.findById _).when(id).returns(IO.pure(None))

      val requestJson = requestData.asJson.toString()
      val request = Request[IO](GET, Uri.unsafeFromString(s"/data/$id")).withEntity(requestJson)
      val response = send(request)

      response.status shouldBe Status.NotFound

    }

    "return all data" in {

      val dataStream = Stream(
        Data(Some(1), 11),
        Data(Some(2), 22),
        Data(Some(3), 33)
      )
      val limit = 10
      val offset = 0

      (repository.get _).when(limit, offset).returns(dataStream)

      val request = Request[IO](GET, Uri.unsafeFromString(s"/data?limit=$limit&offset=$offset"))
      val response = send(request)

      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe dataStream.toList.asJson

    }

    "delete data when id exists" in {

      val id = 1
      (repository.delete _).when(id).returns(IO.pure(Some(())))

      val request = Request[IO](DELETE, Uri.unsafeFromString(s"/data/$id"))
      val response = send(request)

      response.status shouldBe Status.NoContent

    }

    "do not delete data when id does not exist" in {

      val id = 1
      (repository.delete _).when(id).returns(IO.pure(None))

      val request = Request[IO](DELETE, Uri.unsafeFromString(s"/data/$id"))
      val response = send(request)

      response.status shouldBe Status.NotFound

    }

  }

}
