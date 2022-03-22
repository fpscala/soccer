package uz.soccer.services

import cats.effect.Sync
import uz.soccer.db.algebras.UserAlgebra
import uz.soccer.domain.{User, UserData}
import org.typelevel.log4cats.Logger

trait UserService[F[_]] {
  def create(userData: UserData): F[User]
}

object LiveUserService {
  def apply[F[_]: Logger](
    userAlgebra: UserAlgebra[F]
  )(implicit F: Sync[F]): F[LiveUserService[F]] =
    F.delay(
      new LiveUserService[F](userAlgebra)
    )
}

final class LiveUserService[F[_]: Logger](
  userAlgebra: UserAlgebra[F]
)(implicit F: Sync[F])
    extends UserService[F] {

  override def create(userData: UserData): F[User] =
    userAlgebra.create(userData)
}
