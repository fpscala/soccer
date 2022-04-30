package uz.soccer.modules

import cats.data.OptionT
import cats.effect._
import cats.syntax.all._
import dev.profunktor.auth.JwtAuthMiddleware
import dev.profunktor.auth.jwt.JwtToken
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware._
import org.typelevel.log4cats.Logger
import pdi.jwt.JwtClaim
import uz.soccer.config.LogConfig
import uz.soccer.domain.User
import uz.soccer.http.routes.{AuthRoutes, UserRoutes}
import uz.soccer.implicits.CirceDecoderOps
import uz.soccer.security.Security
import uz.soccer.services.redis.RedisClient

import scala.concurrent.duration.DurationInt

object HttpApi {
  def apply[F[_]: Async: Logger](
    security: Security[F],
    redis: RedisClient[F],
    logConfig: LogConfig
  )(implicit F: Sync[F]): HttpApi[F] =
    new HttpApi[F](security, redis, logConfig)
}

final class HttpApi[F[_]: Async: Logger] private (
  security: Security[F],
  redis: RedisClient[F],
  logConfig: LogConfig
) {
  private[this] val baseURL: String = "/"

  def findUser(token: JwtToken): JwtClaim => F[Option[User]] = _ =>
    OptionT(redis.get(token.value))
      .map(_.as[User])
      .value

  private[this] val usersMiddleware =
    JwtAuthMiddleware[F, User](security.userJwtAuth.value, findUser)

  // Auth routes
  private[this] val userRoutes = new UserRoutes[F].routes(usersMiddleware)
  private[this] val authRoutes = AuthRoutes[F](security.auth).routes(usersMiddleware)

  // Open routes
  private[this] val httpRoutes: HttpRoutes[F] =
    userRoutes <+> authRoutes

  private[this] val routes: HttpRoutes[F] = Router(
    baseURL -> httpRoutes
  )

  private[this] val middleware: HttpRoutes[F] => HttpRoutes[F] = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    } andThen { http: HttpRoutes[F] =>
      CORS.policy.withAllowOriginAll
        .withAllowCredentials(false)
        .apply(http)
    } andThen { http: HttpRoutes[F] =>
      Timeout(60.seconds)(http)
    }
  }

  private[this] val loggers: HttpApp[F] => HttpApp[F] = {
    { http: HttpApp[F] =>
      RequestLogger.httpApp(logConfig.httpHeader, logConfig.httpBody)(http)
    } andThen { http: HttpApp[F] =>
      ResponseLogger.httpApp(logConfig.httpHeader, logConfig.httpBody)(http)
    }
  }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)

}
