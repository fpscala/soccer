package uz.soccer.stub_services

import cats.effect.Sync
import uz.soccer.domain.{Stadium, Team, types}
import uz.soccer.effects.GenUUID
import uz.soccer.services.{Stadiums, Teams}

class StadiumsStub[F[_]: Sync: GenUUID] extends Stadiums[F] {
  override def create(stadium: Stadium.CreateStadium): F[Stadium] = ???
  override def update(stadium: Stadium): F[Unit]                  = ???
  override def getAll: F[List[Stadium]]                           = ???
  override def delete(uuid: types.StadiumId): F[Unit]             = ???
}
