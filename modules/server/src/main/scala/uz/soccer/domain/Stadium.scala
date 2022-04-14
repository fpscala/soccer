package uz.soccer.domain
import derevo.cats.show
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import uz.soccer.domain.custom.refinements.Tel
import uz.soccer.domain.types.{Address, Owner, StadiumId}
import io.circe.refined._
import eu.timepit.refined.cats._

@derive(decoder, encoder, show)
case class Stadium (uuid: StadiumId, address: Address, owner: Owner, tel: Tel)

object Stadium {
  @derive(decoder, encoder, show)
  case class CreateStadium (address: Address, owner: Owner, tel: Tel)
}
