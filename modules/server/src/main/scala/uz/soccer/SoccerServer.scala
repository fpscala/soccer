package uz.soccer

import cats.effect._
import cats.effect.std.Console
import cats.implicits._
import uz.soccer.config.{ConfigLoader, HttpServerConfig}
import uz.soccer.modules._
import uz.soccer.services.redis.RedisClient
import eu.timepit.refined.auto.autoUnwrap
import org.http4s._
import org.http4s.blaze.server.BlazeServerBuilder
import org.typelevel.log4cats.Logger

import scala.concurrent.ExecutionContext.global

object SoccerServer {

  def run[F[_]: Async: Console: Logger]: F[ExitCode] =
    for {
      conf     <- ConfigLoader.app[F]
      db       <- Database[F](conf.dbConfig)
      redis    <- RedisClient[F](conf.redisConfig)
      programs <- SoccerProgram[F](db, redis)
      httpAPI  <- HttpApi[F](programs, conf.logConfig)
      _        <- server[F](conf.serverConfig, httpAPI.httpApp)
    } yield ExitCode.Success

  private[this] def server[F[_]: Async](
    conf: HttpServerConfig,
    httpApp: HttpApp[F]
  ): F[Unit] =
    BlazeServerBuilder[F]
      .withExecutionContext(global)
      .bindHttp(conf.port, conf.host)
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
}