package uz.soccer.http.routes

import cats.MonadThrow
import cats.implicits.{catsSyntaxApplicativeError, catsSyntaxFlatMapOps, toFlatMapOps}
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import uz.soccer.domain.Match.CreateMatch
import uz.soccer.domain.custom.exception.{DateTimeInCorrect, StadiumIdInCorrect}
import uz.soccer.domain.types.MatchId
import uz.soccer.domain.{Match, User}
import uz.soccer.services.Matches

final case class MatchRoutes[F[_]: JsonDecoder: MonadThrow](matches: Matches[F]) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/match"

  private[this] val httpRoutes: AuthedRoutes[User, F] = AuthedRoutes.of {
    case GET -> Root as _ =>
      matches.getAll.flatMap(Ok(_))

    case ar @ POST -> Root as _ =>
      ar.req
        .decodeR[CreateMatch] { createMatch =>
          matches.create(createMatch).flatMap(Created(_))
        }
        .recoverWith {
          case error: DateTimeInCorrect =>
            BadRequest(s"${error.times.map(_.toString).reduce(_ + " or " + _)} isn't correct")
          case StadiumIdInCorrect(uuid) =>
            BadRequest(s"Identification $uuid isn't correct")
        }

    case ar @ PUT -> Root as _ =>
      ar.req.decodeR[Match] { `match` =>
        matches.update(`match`) >> NoContent()
      }

    case DELETE -> Root / UUIDVar(uuid) as _ =>
      matches.delete(MatchId(uuid)) >> NoContent()
  }

  def routes(authMiddleware: AuthMiddleware[F, User]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
