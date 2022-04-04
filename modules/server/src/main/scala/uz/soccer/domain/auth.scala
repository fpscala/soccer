package uz.soccer.domain

import derevo.cats._
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import uz.soccer.domain.custom.refinements.{EmailAddress, Password}
import uz.soccer.types.uuid
import io.circe.refined._
import eu.timepit.refined.cats._

import java.util.UUID
import javax.crypto.Cipher
import scala.util.control.NoStackTrace

object auth {

  @derive(decoder, encoder, eqv, show, uuid)
  @newtype
  case class UserId(value: UUID)

  @derive(decoder, encoder, eqv, show)
  @newtype
  case class EncryptedPassword(value: String)

  @newtype
  case class EncryptCipher(value: Cipher)

  @newtype
  case class DecryptCipher(value: Cipher)

  // --------- user registration -----------

  @derive(decoder, encoder, show)
  @newtype
  case class UserName(value: NonEmptyString)

  @derive(decoder, encoder, show)
  case class CreateUser(
    name: UserName,
    email: EmailAddress,
    gender: Gender,
    password: Password
  )

  case class UserNotFound(email: EmailAddress)    extends NoStackTrace
  case class EmailInUse(email: EmailAddress)   extends NoStackTrace
  case class InvalidPassword(email: EmailAddress) extends NoStackTrace
  case object UnsupportedOperation               extends NoStackTrace

  case object TokenNotFound extends NoStackTrace
}
