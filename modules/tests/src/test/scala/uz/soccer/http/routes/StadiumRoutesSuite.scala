package uz.soccer.http.routes

import cats.effect.{IO, Sync}
import cats.implicits._
import org.http4s.Method.{DELETE, GET, POST, PUT}
import org.http4s.client.dsl.io._
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{Status, Uri}
import uz.soccer.domain.Stadium.CreateStadium
import uz.soccer.domain.types.StadiumId
import uz.soccer.domain.{ID, Stadium}
import uz.soccer.effects.GenUUID
import uz.soccer.stub_services.StadiumsStub
import uz.soccer.utils.Generators._
import uz.soccer.utils.HttpSuite

object StadiumRoutesSuite extends HttpSuite {
  def stadiums[F[_]: Sync: GenUUID]: StadiumsStub[F] = new StadiumsStub[F] {
    override def create(createStadium: CreateStadium): F[Stadium] =
      ID.make[F, StadiumId].map { stadiumId =>
        Stadium(stadiumId, createStadium.address, createStadium.owner, createStadium.tel)
      }

    override def getAll: F[List[Stadium]]           = List.empty[Stadium].pure[F]
    override def update(team: Stadium): F[Unit]     = Sync[F].unit
    override def delete(teamId: StadiumId): F[Unit] = Sync[F].unit
  }

  test("POST Create stadium") {
    val gen = for {
      u <- userGen
      s <- createStadiumGen
    } yield (u, s)

    forall(gen) { case (user, createStadium) =>
      for {
        token <- authToken(user)
        req    = POST(createStadium, uri"/stadium").putHeaders(token)
        routes = StadiumRoutes[IO](stadiums).routes(usersMiddleware)
        res <- expectHttpStatus(routes, req)(Status.Created)
      } yield res
    }
  }

  test("GET all stadiums") {
    forall(userGen) { user =>
      for {
        token <- authToken(user)
        req    = GET(uri"/stadium").putHeaders(token)
        routes = StadiumRoutes[IO](stadiums).routes(usersMiddleware)
        res <- expectHttpStatus(routes, req)(Status.Ok)
      } yield res
    }
  }

  test("PUT update stadium") {
    val gen = for {
      u <- userGen
      s <- stadiumGen
    } yield (u, s)
    forall(gen) { case (user, stadium) =>
      for {
        token <- authToken(user)
        req    = PUT(stadium, uri"/stadium").putHeaders(token)
        routes = StadiumRoutes[IO](stadiums).routes(usersMiddleware)
        res <- expectHttpStatus(routes, req)(Status.NoContent)
      } yield res
    }
  }

  test("DELETE stadium") {
    val gen = for {
      u <- userGen
      s <- stadiumIdGen
    } yield (u, s)
    forall(gen) { case (user, stadiumId) =>
      for {
        token <- authToken(user)
        req    = DELETE(Uri.unsafeFromString(s"/stadium/$stadiumId")).putHeaders(token)
        routes = StadiumRoutes[IO](stadiums).routes(usersMiddleware)
        res <- expectHttpStatus(routes, req)(Status.NoContent)
      } yield res
    }
  }

}
