package uz.soccer.stub_services

import uz.soccer.domain.User
import uz.soccer.domain.User.{CreateUser, UserWithPassword}
import uz.soccer.domain.custom.refinements.EmailAddress
import uz.soccer.domain.types.EncryptedPassword
import uz.soccer.services.Users

class UsersStub[F[_]] extends Users[F] {
  override def find(email: EmailAddress): F[Option[UserWithPassword]]              = ???
  override def create(userParam: CreateUser, password: EncryptedPassword): F[User] = ???
}
