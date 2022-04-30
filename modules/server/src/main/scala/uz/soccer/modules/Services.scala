package uz.soccer.modules
import cats.effect.Sync
import cats.effect.kernel.Resource
import skunk.Session
import uz.soccer.services.{Matches, Stadiums, Teams, Users}

object Services {
  def apply[F[_]: Sync](implicit session: Resource[F, Session[F]]): Services[F] =
    new Services[F](Users[F], Matches[F], Teams[F], Stadiums[F])
}

final class Services[F[_]: Sync] private (
  val users: Users[F],
  val matches: Matches[F],
  val teams: Teams[F],
  val stadiums: Stadiums[F]
)
