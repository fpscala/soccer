package uz.soccer.http.routes

import cats.MonadThrow
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, _}
import uz.soccer.domain.User

final class UserRoutes[F[_]: JsonDecoder: MonadThrow] extends Http4sDsl[F] {

  private[routes] val prefixPath = "/user"

  private[this] val httpRoutes: AuthedRoutes[User, F] = AuthedRoutes.of { case GET -> Root as user =>
    Ok(user)
  }

  def routes(authMiddleware: AuthMiddleware[F, User]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
