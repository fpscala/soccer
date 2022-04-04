package uz.soccer.http.auth

import derevo.cats.show
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import dev.profunktor.auth.jwt._
import io.estatico.newtype.macros.newtype
import uz.soccer.domain.{Gender, Role}
import uz.soccer.domain.auth._
import uz.soccer.domain.custom.refinements.EmailAddress
import io.circe.refined._
import eu.timepit.refined.cats._

object users {

  @newtype case class AdminJwtAuth(value: JwtSymmetricAuth)
  @newtype case class UserJwtAuth(value: JwtSymmetricAuth)

  @derive(decoder, encoder, show)
  case class User(id: UserId, name: UserName, email: EmailAddress, gender: Gender, role: Role)

  @derive(decoder, encoder)
  case class UserWithPassword(user: User, password: EncryptedPassword)


}
