package uz.soccer.http.routes

import cats.MonadThrow
import cats.implicits.{catsSyntaxApplicativeError, catsSyntaxFlatMapOps, toFlatMapOps}
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import uz.soccer.domain.Team.CreateTeam
import uz.soccer.domain.custom.exception.TeamNameInUse
import uz.soccer.domain.types.{TeamId, TeamName, UserId}
import uz.soccer.domain.{Team, User}
import uz.soccer.services.Teams

final case class TeamRoutes[F[_]: JsonDecoder: MonadThrow](teams: Teams[F]) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/team"

  private[this] val httpRoutes: AuthedRoutes[User, F] = AuthedRoutes.of {
    case GET -> Root as _ =>
      teams.getAll.flatMap(Ok(_))

    case GET -> Root / UUIDVar(uuid) as _ =>
      teams.getByUserId(UserId(uuid)).flatMap(Ok(_))

    case ar @ POST -> Root as _ =>
      ar.req
        .decodeR[CreateTeam] { createTeam =>
          teams.create(createTeam.name).flatMap(Created(_))
        }
        .recoverWith { case TeamNameInUse(name) =>
          Conflict(s"$name: already used")
        }

    case ar @ PUT -> Root as _ =>
      ar.req.decodeR[Team] { team =>
        teams.update(team) >> NoContent()
      }

    case DELETE -> Root / UUIDVar(uuid) as _ =>
      teams.delete(TeamId(uuid)) >> NoContent()
  }

  def routes(authMiddleware: AuthMiddleware[F, User]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
