package uz.soccer.services

import cats.data.OptionT
import cats.effect._
import cats.syntax.all._
import skunk._
import skunk.implicits._
import uz.soccer.domain.{ID, User}
import uz.soccer.domain.User._
import uz.soccer.domain.custom.exception.EmailInUse
import uz.soccer.domain.custom.refinements.EmailAddress
import uz.soccer.domain.types.{EncryptedPassword, UserId}
import uz.soccer.effects.GenUUID
import uz.soccer.services.sql.UserSQL._

trait Users[F[_]] {
  def find(email: EmailAddress): F[Option[UserWithPassword]]
  def create(userParam: CreateUser, password: EncryptedPassword): F[User]
}

object Users {
  def apply[F[_]: GenUUID: Sync](
    session: Resource[F, Session[F]]
  ): Users[F] =
    new Users[F] with SkunkHelper[F] {
      implicit val ev: Resource[F, Session[F]] = session
      def find(email: EmailAddress): F[Option[UserWithPassword]] =
        OptionT(prepOptQuery(selectUser, email)).map { case user ~ p =>
          UserWithPassword(user, p)
        }.value

      def create(userParam: CreateUser, password: EncryptedPassword): F[User] =
        ID.make[F, UserId]
          .flatMap { id =>
            prepQueryUnique(insertUser, id ~ userParam ~ password).map(_._1)
          }
          .recoverWith { case SqlState.UniqueViolation(_) =>
            EmailInUse(userParam.email).raiseError[F, User]
          }
    }

}
