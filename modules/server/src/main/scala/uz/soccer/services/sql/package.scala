package uz.soccer.services

import eu.timepit.refined.types.string.NonEmptyString
import skunk.Codec
import skunk.codec.all.{uuid, varchar}
import skunk.data.{Arr, Type}
import uz.soccer.domain.{Gender, Role}
import uz.soccer.domain.auth.{EncryptedPassword, UserId, UserName}
import uz.soccer.domain.custom.refinements.EmailAddress
import eu.timepit.refined.auto.autoUnwrap

import java.util.UUID
import scala.util.Try

package object sql {

  def parseUUID: String => Either[String, UUID] = s =>
    Try(Right(UUID.fromString(s))).getOrElse(Left(s"Invalid argument: [ $s ]"))

  val _uuid: Codec[Arr[UUID]] = Codec.array(_.toString, parseUUID, Type._uuid)

  val listUUID: Codec[List[UUID]] = _uuid.imap(_.flattenTo(List))(l => Arr(l: _*))

  val userId: Codec[UserId] = uuid.imap[UserId](UserId.apply)(_.value)

  val userName: Codec[UserName] = varchar.imap[UserName](name => UserName(NonEmptyString.unsafeFrom(name)))(_.value)

  val encPassword: Codec[EncryptedPassword] = varchar.imap[EncryptedPassword](EncryptedPassword.apply)(_.value)

  val email: Codec[EmailAddress] = varchar.imap[EmailAddress](EmailAddress.unsafeFrom)(_.value)

  val gender: Codec[Gender] = varchar.imap[Gender](Gender.unsafeFrom)(_.value)

  val role: Codec[Role] = varchar.imap[Role](Role.unsafeFrom)(_.value)

}
