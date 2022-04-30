package uz.soccer.stub_services

import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt
import uz.soccer.domain.User
import uz.soccer.domain.User.{CreateUser, UserWithPassword}
import uz.soccer.domain.custom.refinements.EmailAddress
import uz.soccer.services.Users

class UsersStub[F[_]] extends Users[F] {
  override def find(email: EmailAddress): F[Option[UserWithPassword]]                 = ???
  override def create(userParam: CreateUser, password: PasswordHash[SCrypt]): F[User] = ???
}
