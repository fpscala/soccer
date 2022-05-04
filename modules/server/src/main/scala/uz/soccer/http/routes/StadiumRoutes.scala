package uz.soccer.http.routes

import cats.MonadThrow
import cats.implicits.{catsSyntaxFlatMapOps, toFlatMapOps}
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import uz.soccer.domain.Stadium.CreateStadium
import uz.soccer.domain.types.StadiumId
import uz.soccer.domain.{Stadium, User}
import uz.soccer.services.Stadiums

final case class StadiumRoutes[F[_]: JsonDecoder: MonadThrow](stadiums: Stadiums[F]) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/stadium"

  private[this] val httpRoutes: AuthedRoutes[User, F] = AuthedRoutes.of {
    case GET -> Root as _ =>
      stadiums.getAll.flatMap(Ok(_))

    case ar @ POST -> Root as _ =>
      ar.req.decodeR[CreateStadium] { createTeam =>
        stadiums.create(createTeam).flatMap(Created(_))
      }

    case ar @ PUT -> Root as _ =>
      ar.req.decodeR[Stadium] { team =>
        stadiums.update(team) >> NoContent()
      }

    case DELETE -> Root / UUIDVar(uuid) as _ =>
      stadiums.delete(StadiumId(uuid)) >> NoContent()
  }

  def routes(authMiddleware: AuthMiddleware[F, User]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
