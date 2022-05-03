package uz.soccer.stub_services
import cats.effect.Sync
import uz.soccer.domain.{Team, types}
import uz.soccer.effects.GenUUID
import uz.soccer.services.Teams

class TeamsStub[F[_]: Sync: GenUUID] extends Teams[F] {
  override def create(teamName: types.TeamName): F[Team]      = ???
  override def update(team: Team): F[Unit]                    = ???
  override def getAll: F[List[Team]]                          = ???
  override def getByUserId(uuid: types.UserId): F[List[Team]] = ???
  override def delete(uuid: types.TeamId): F[Unit]            = ???
}
