package uz.soccer.services

import eu.timepit.refined.types.string.NonEmptyString
import skunk.Codec
import skunk.codec.all.{uuid, varchar}
import skunk.data.{Arr, Type}
import uz.soccer.domain.custom.refinements.{EmailAddress, Tel}
import uz.soccer.domain.types.{Address, EncryptedPassword, Owner, TeamName, UserName}
import uz.soccer.domain.{Gender, Role}
import uz.soccer.types.IsUUID
import eu.timepit.refined.auto.autoUnwrap

import java.util.UUID
import scala.util.Try

package object sql {

  def parseUUID: String => Either[String, UUID] = s =>
    Try(Right(UUID.fromString(s))).getOrElse(Left(s"Invalid argument: [ $s ]"))

  val _uuid: Codec[Arr[UUID]] = Codec.array(_.toString, parseUUID, Type._uuid)

  val listUUID: Codec[List[UUID]] = _uuid.imap(_.flattenTo(List))(l => Arr(l: _*))

  def identity[A: IsUUID]: Codec[A] = uuid.imap[A](IsUUID[A]._UUID.get)(IsUUID[A]._UUID.apply)

  val userName: Codec[UserName] = varchar.imap[UserName](name => UserName(NonEmptyString.unsafeFrom(name)))(_.value)

  val teamName: Codec[TeamName] = varchar.imap[TeamName](name => TeamName(NonEmptyString.unsafeFrom(name)))(_.value)

  val encPassword: Codec[EncryptedPassword] = varchar.imap[EncryptedPassword](EncryptedPassword.apply)(_.value)

  val email: Codec[EmailAddress] = varchar.imap[EmailAddress](EmailAddress.unsafeFrom)(_.value)

  val tel: Codec[Tel] = varchar.imap[Tel](Tel.unsafeFrom)(_.value)

  val address: Codec[Address] = varchar.imap[Address](str => Address(NonEmptyString.unsafeFrom(str)))(_.value)

  val owner: Codec[Owner] = varchar.imap[Owner](str => Owner(NonEmptyString.unsafeFrom(str)))(_.value)

  val gender: Codec[Gender] = varchar.imap[Gender](Gender.unsafeFrom)(_.value)

  val role: Codec[Role] = varchar.imap[Role](Role.unsafeFrom)(_.value)

}
