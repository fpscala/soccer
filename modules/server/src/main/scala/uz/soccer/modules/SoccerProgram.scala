package uz.soccer.modules

import cats.effect._
import cats.implicits._
import uz.soccer.db.algebras.Algebras
import uz.soccer.services.redis.RedisClient
import uz.soccer.services.{LiveUserService, UserService}
import org.typelevel.log4cats.Logger

object SoccerProgram {
  def apply[F[_]: Sync: Async: Logger](
    database: Database[F],
    redisClient: RedisClient[F]
  ): F[SoccerProgram[F]] = {
    implicit val redis: RedisClient[F] = redisClient

    def algebrasF: F[Algebras[F]] = (
      database.user
    ).map(Algebras.apply)

    for {
      algebras <- algebrasF
      auth <- Authentication[F](algebras.user)
      userService <- LiveUserService[F](algebras.user)
    } yield new SoccerProgram[F](auth, userService)
  }
}

final class SoccerProgram[F[_]: Sync] private (
  val auth: Authentication[F],
  val userService: UserService[F]
)
