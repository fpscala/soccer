package uz.soccer.security

import cats.effect._
import cats.syntax.all._
import dev.profunktor.auth.jwt._
import eu.timepit.refined.auto._
import pdi.jwt._
import skunk.Session
import uz.soccer.config.AppConfig
import uz.soccer.http.auth.users._
import uz.soccer.services.redis.RedisClient
import uz.soccer.services.{Auth, Users, UsersAuth}

object Security {
  def apply[F[_]: Sync](
    cfg: AppConfig,
    session: Resource[F, Session[F]],
    redis: RedisClient[F]
  ): F[Security[F]] = {
    val userJwtAuth: UserJwtAuth =
      UserJwtAuth(JwtAuth.hmac(cfg.jwtConfig.tokenConfig.value.secret, JwtAlgorithm.HS256))

    for {
      tokens <- JwtExpire[F].map(Tokens.make[F](_, cfg.jwtConfig.tokenConfig.value, cfg.jwtConfig.tokenExpiration))
      crypto <- Crypto[F](cfg.jwtConfig.passwordSalt.value)
      users     = Users[F](session)
      auth      = Auth[F](cfg.jwtConfig.tokenExpiration, tokens, users, redis, crypto)
      usersAuth = UsersAuth[F](redis)
    } yield new Security[F](auth, usersAuth, userJwtAuth)

  }
}

final class Security[F[_]] private (
  val auth: Auth[F],
  val usersAuth: UsersAuth[F, User],
  val userJwtAuth: UserJwtAuth
)
