package uz.soccer.services
import uz.soccer.domain.Stadium
import uz.soccer.domain.Stadium.CreateStadium

trait Stadiums[F[_]] {
  def create(stadiums: CreateStadium): F[Stadium]
}
