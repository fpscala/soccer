package uz.soccer.http.routes

import cats.effect.{Async, Sync}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger
import uz.soccer.utils.FileLoader

object RootRoutes {
  def apply[F[_]: Async: Sync: Logger]: RootRoutes[F] =
    new RootRoutes[F]
}

class RootRoutes[F[_]: Async: Logger](implicit F: Sync[F]) {
  private[this] val supportedStaticExtensions =
    List(".css", ".png", ".ico", ".js", ".jpg", ".jpeg", ".otf", ".ttf", ".woff2", ".woff")

  implicit object dsl extends Http4sDsl[F]; import dsl._

  val routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case request @ GET -> Root =>
        FileLoader[F].page("index.html", request)
      case request if supportedStaticExtensions.exists(request.pathInfo.toString.endsWith) =>
        FileLoader[F].assets(request.pathInfo.toString, request)
    }

}
