package uz.soccer.modules

import cats.effect._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware._
import org.http4s.server.staticcontent.webjarServiceBuilder
import org.typelevel.log4cats.Logger
import uz.soccer.config.LogConfig
import uz.soccer.http.routes.RootRoutes

import scala.concurrent.duration.DurationInt

object HttpApi {
  def apply[F[_]: Async: Logger](
    logConfig: LogConfig
  )(implicit F: Sync[F]): HttpApi[F] =
    new HttpApi[F](logConfig)
}

final class HttpApi[F[_]: Async: Logger] private (
  logConfig: LogConfig
) {
  private[this] val root: String              = "/"
  private[this] val assets: String            = "/assets"
  private[this] val rootRoutes: HttpRoutes[F] = RootRoutes[F].routes
  private[this] val webjars: HttpRoutes[F]    = webjarServiceBuilder[F].toRoutes

  private val routes: HttpRoutes[F] = Router(
    root   -> rootRoutes,
    assets -> webjars
  )

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = {
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

  private val loggers: HttpApp[F] => HttpApp[F] = {
    { http: HttpApp[F] =>
      RequestLogger.httpApp(logConfig.httpHeader, logConfig.httpBody)(http)
    } andThen { http: HttpApp[F] =>
      ResponseLogger.httpApp(logConfig.httpHeader, logConfig.httpBody)(http)
    }
  }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)

}
