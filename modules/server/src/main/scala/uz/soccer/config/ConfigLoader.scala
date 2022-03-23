package uz.soccer.config

import cats.effect.Async
import cats.implicits._
import ciris._
import ciris.refined.refTypeConfigDecoder
import eu.timepit.refined.cats._
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import uz.soccer.domain.custom.refinements.UriAddress
import uz.soccer.types._

import scala.concurrent.duration.FiniteDuration

object ConfigLoader {
  private[this] def databaseConfig: ConfigValue[Effect, DBConfig] = (
    env("POSTGRES_HOST").as[NonEmptyString],
    env("POSTGRES_PORT").as[UserPortNumber],
    env("POSTGRES_USER").as[NonEmptyString],
    env("POSTGRES_PASSWORD").as[NonEmptyString].secret,
    env("POSTGRES_DATABASE").as[NonEmptyString],
    env("POSTGRES_POOL_SIZE").as[PosInt]
  ).parMapN(DBConfig.apply)

  private[this] def httpLogConfig: ConfigValue[Effect, LogConfig] = (
    env("HTTP_HEADER_LOG").as[Boolean],
    env("HTTP_BODY_LOG").as[Boolean]
  ).parMapN(LogConfig.apply)

  private[this] def httpServerConfig: ConfigValue[Effect, HttpServerConfig] = (
    env("HTTP_HOST").as[NonEmptyString],
    env("HTTP_PORT").as[UserPortNumber]
  ).parMapN(HttpServerConfig.apply)

  private[this] def redisConfig: ConfigValue[Effect, RedisConfig] =
    env("REDIS_SERVER_URI").as[UriAddress].map(RedisConfig.apply)

  private[this] def tokenExpirationConfig: ConfigValue[Effect, TokenExpiration] =
    env("JWT_TOKEN_EXPIRATION").as[FiniteDuration].map(TokenExpiration.apply)

  private[this] def adminJwtConfig: ConfigValue[Effect, AdminJwtConfig] = (
    env("JWT_SECRET_KEY").as[JwtSecretKeyConfig].secret,
    env("ADMIN_USER_TOKEN").as[AdminUserTokenConfig].secret
  ).parMapN(AdminJwtConfig.apply)

  def load[F[_]: Async]: F[AppConfig] = (
    adminJwtConfig,
    env("ACCESS_TOKEN_SECRET_KEY").as[JwtAccessTokenKeyConfig].secret,
    env("PASSWORD_SALT").as[PasswordSalt].secret,
    tokenExpirationConfig,
    databaseConfig,
    redisConfig,
    httpServerConfig,
    httpLogConfig
  ).parMapN(AppConfig.apply).load[F]
}
