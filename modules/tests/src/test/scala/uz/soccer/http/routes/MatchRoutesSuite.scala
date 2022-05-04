package uz.soccer.http.routes

import cats.effect.{IO, Sync}
import cats.implicits._
import org.http4s.Method.{DELETE, GET, POST, PUT}
import org.http4s.client.dsl.io._
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{Status, Uri}
import uz.soccer.domain.custom.exception.{DateTimeInCorrect, StadiumIdInCorrect}
import uz.soccer.domain.types.MatchId
import uz.soccer.domain.{ID, Match, types}
import uz.soccer.effects.GenUUID
import uz.soccer.stub_services.MatchesStub
import uz.soccer.utils.Generators._
import uz.soccer.utils.HttpSuite

object MatchRoutesSuite extends HttpSuite {
  def matches[F[_]: Sync: GenUUID](
    dateInCorrect: Boolean = false,
    stadiumIdInCorrect: Boolean = false
  ): MatchesStub[F] = new MatchesStub[F] {
    override def create(`match`: Match.CreateMatch): F[Match] =
      if (dateInCorrect)
        Sync[F].raiseError[Match](DateTimeInCorrect(`match`.startTime, `match`.endTime))
      else if (stadiumIdInCorrect)
        Sync[F].raiseError[Match](StadiumIdInCorrect(`match`.stadiumId))
      else
        ID.make[F, MatchId].map { matchId =>
          Match(matchId, `match`.startTime, `match`.endTime, `match`.stadiumId)
        }

    override def update(`match`: Match): F[Unit]      = Sync[F].unit
    override def getAll: F[List[Match]]               = List.empty[Match].pure[F]
    override def delete(uuid: types.MatchId): F[Unit] = Sync[F].unit
  }

  test("POST Create stadium") {
    val gen = for {
      u  <- userGen
      m  <- createMatchGen
      d  <- booleanGen
      si <- booleanGen
    } yield (u, m, d, si)

    forall(gen) { case (user, createMatch, dateInCorrect, sIdInCorrect) =>
      for {
        token <- authToken(user)
        req          = POST(createMatch, uri"/match").putHeaders(token)
        routes       = MatchRoutes[IO](matches(dateInCorrect, sIdInCorrect)).routes(usersMiddleware)
        shouldReturn = if (dateInCorrect | sIdInCorrect) Status.BadRequest else Status.Created
        res <- expectHttpStatus(routes, req)(shouldReturn)
      } yield res
    }
  }

  test("GET all matches") {
    forall(userGen) { user =>
      for {
        token <- authToken(user)
        req    = GET(uri"/match").putHeaders(token)
        routes = MatchRoutes[IO](matches()).routes(usersMiddleware)
        res <- expectHttpStatus(routes, req)(Status.Ok)
      } yield res
    }
  }

  test("PUT update match") {
    val gen = for {
      u <- userGen
      m <- matchGen
    } yield (u, m)
    forall(gen) { case (user, matchV) =>
      for {
        token <- authToken(user)
        req    = PUT(matchV, uri"/match").putHeaders(token)
        routes = MatchRoutes[IO](matches()).routes(usersMiddleware)
        res <- expectHttpStatus(routes, req)(Status.NoContent)
      } yield res
    }
  }

  test("DELETE match") {
    val gen = for {
      u <- userGen
      m <- matchIdGen
    } yield (u, m)
    forall(gen) { case (user, matchId) =>
      for {
        token <- authToken(user)
        req    = DELETE(Uri.unsafeFromString(s"/match/$matchId")).putHeaders(token)
        routes = MatchRoutes[IO](matches()).routes(usersMiddleware)
        res <- expectHttpStatus(routes, req)(Status.NoContent)
      } yield res
    }
  }

}
