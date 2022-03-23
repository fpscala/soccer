package uz.soccer.modules

import cats.effect._
import cats.syntax.all._
import dev.profunktor.auth.JwtAuthMiddleware
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware._
import org.http4s.server.staticcontent.webjarServiceBuilder
import org.typelevel.log4cats.Logger
import uz.soccer.config.LogConfig
import uz.soccer.http.auth.users.CommonUser
import uz.soccer.http.routes.{LoginRoutes, LogoutRoutes, RootRoutes, UserRoutes}
import uz.soccer.security.Security

import scala.concurrent.duration.DurationInt

object HttpApi {
  def apply[F[_]: Async: Logger](
    security: Security[F],
    logConfig: LogConfig
  )(implicit F: Sync[F]): HttpApi[F] =
    new HttpApi[F](security, logConfig)
}

final class HttpApi[F[_]: Async: Logger] private (
  security: Security[F],
  logConfig: LogConfig
) {
  private[this] val root: String   = "/"
  private[this] val assets: String = "/assets"

  private val usersMiddleware =
    JwtAuthMiddleware[F, CommonUser](security.userJwtAuth.value, security.usersAuth.findUser)

  // Auth routes
  private val loginRoutes  = LoginRoutes[F](security.auth).routes
  private val userRoutes = UserRoutes[F](security.auth).routes
  private val logoutRoutes = LogoutRoutes[F](security.auth).routes(usersMiddleware)

  // Open routes
  private[this] val rootRoutes: HttpRoutes[F] = RootRoutes[F].routes
  private[this] val webjars: HttpRoutes[F]    = webjarServiceBuilder[F].toRoutes

  private[this] val openRoutes: HttpRoutes[F] =
    userRoutes <+> loginRoutes <+> logoutRoutes <+> rootRoutes

  private[this] val routes: HttpRoutes[F] = Router(
    root   -> openRoutes,
    assets -> webjars
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
