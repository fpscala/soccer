package uz.soccer
import eu.timepit.refined.types.string.NonEmptyString

package object types {
  @derive(configDecoder, show)
  @newtype
  case class PasswordSalt(secret: NonEmptyString)
}
