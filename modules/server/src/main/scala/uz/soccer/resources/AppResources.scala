package uz.soccer.resources

import cats.effect.std.Console
import cats.effect.{Concurrent, Resource}
import cats.syntax.all._
import dev.profunktor.redis4cats.effect.MkRedis
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import eu.timepit.refined.auto._
import fs2.io.net.Network
import natchez.Trace.Implicits.noop
import org.typelevel.log4cats.Logger
import skunk._
import skunk.codec.text._
import skunk.implicits._
import uz.soccer.config.{AppConfig, DBConfig, RedisConfig}

case class AppResources[F[_]](
  postgres: Resource[F, Session[F]],
  redis: RedisCommands[F, String, String]
)

object AppResources {

  def apply[F[_]: Concurrent: Console: Logger: MkRedis: Network](
    cfg: AppConfig
  ): Resource[F, AppResources[F]] = {

    def checkPostgresConnection(
      postgres: Resource[F, Session[F]]
    ): F[Unit] =
      postgres.use { session =>
        session.unique(sql"select version();".query(text)).flatMap { v =>
          Logger[F].info(s"Connected to Postgres $v")
        }
      }

    def checkRedisConnection(
      redis: RedisCommands[F, String, String]
    ): F[Unit] =
      redis.info.flatMap {
        _.get("redis_version").traverse_ { v =>
          Logger[F].info(s"Connected to Redis $v")
        }
      }

    def mkPostgreSqlResource(c: DBConfig): SessionPool[F] =
      Session
        .pooled[F](
          host = c.host,
          port = c.port,
          user = c.user,
          password = Some(c.password.value),
          database = c.database,
          max = c.poolSize
        )
        .evalTap(checkPostgresConnection)

    def mkRedisResource(c: RedisConfig): Resource[F, RedisCommands[F, String, String]] =
      Redis[F].utf8(c.uri.value).evalTap(checkRedisConnection)

    (
      mkPostgreSqlResource(cfg.dbConfig),
      mkRedisResource(cfg.redis)
    ).parMapN(AppResources[F])

  }

}
