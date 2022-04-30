package uz.soccer.services

import cats.effect.Sync
import cats.syntax.all._
import dev.profunktor.auth.jwt.JwtToken
import eu.timepit.refined.auto.autoUnwrap
import tsec.passwordhashers.jca.SCrypt
import uz.soccer.domain.User._
import uz.soccer.domain._
import uz.soccer.domain.custom.exception.{EmailInUse, InvalidPassword, UserNotFound}
import uz.soccer.domain.custom.refinements.EmailAddress
import uz.soccer.implicits.GenericTypeOps
import uz.soccer.security.Tokens
import uz.soccer.services.redis.RedisClient
import uz.soccer.types.TokenExpiration

trait Auth[F[_]] {
  def newUser(userParam: CreateUser): F[JwtToken]
  def login(credentials: Credentials): F[JwtToken]
  def logout(token: JwtToken, email: EmailAddress): F[Unit]
}

object Auth {
  def apply[F[_]: Sync](
    tokenExpiration: TokenExpiration,
    tokens: Tokens[F],
    users: Users[F],
    redis: RedisClient[F]
  ): Auth[F] =
    new Auth[F] {

      private val TokenExpiration = tokenExpiration.value

      override def newUser(userParam: CreateUser): F[JwtToken] =
        users.find(userParam.email).flatMap {
          case Some(_) =>
            EmailInUse(userParam.email).raiseError[F, JwtToken]
          case None =>
            for {
              hash <- SCrypt.hashpw[F](userParam.password)
              user <- users.create(userParam, hash)
              t    <- tokens.create
              _    <- redis.put(t.value, user, TokenExpiration)
              _    <- redis.put(user.email, t.value, TokenExpiration)
            } yield t
        }

      def login(credentials: Credentials): F[JwtToken] =
        users.find(credentials.email).flatMap {
          case None =>
            UserNotFound(credentials.email).raiseError[F, JwtToken]
          case Some(user) if !SCrypt.checkpwUnsafe(credentials.password, user.password) =>
            InvalidPassword(credentials.email).raiseError[F, JwtToken]
          case Some(userWithPass) =>
            redis.get(credentials.email).flatMap {
              case Some(t) => JwtToken(t).pure[F]
              case None =>
                tokens.create.flatTap { t =>
                  redis.put(t.value, userWithPass.user, TokenExpiration) *>
                    redis.put(credentials.email, t.value, TokenExpiration)
                }
            }
        }

      def logout(token: JwtToken, email: EmailAddress): F[Unit] =
        redis.del(token.show, email)

    }
}
