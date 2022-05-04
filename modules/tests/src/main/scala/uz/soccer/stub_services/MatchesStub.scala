package uz.soccer.stub_services

import cats.effect.Sync
import uz.soccer.domain.{Match, Stadium, types}
import uz.soccer.effects.GenUUID
import uz.soccer.services.{Matches, Stadiums}

class MatchesStub[F[_]: Sync: GenUUID] extends Matches[F] {
  override def create(`match`: Match.CreateMatch): F[Match] = ???
  override def update(`match`: Match): F[Unit]              = ???
  override def getAll: F[List[Match]]                       = ???
  override def delete(uuid: types.MatchId): F[Unit]         = ???
}
