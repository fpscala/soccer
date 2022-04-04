package uz.soccer.stub_services

import uz.soccer.domain.auth
import uz.soccer.domain.auth.EncryptedPassword
import uz.soccer.domain.custom.refinements.EmailAddress
import uz.soccer.http.auth.users
import uz.soccer.http.auth.users.UserWithPassword
import uz.soccer.services.Users

class UsersStub[F[_]] extends Users[F] {
  override def find(email: EmailAddress): F[Option[UserWithPassword]] = ???
  override def create(userParam: auth.CreateUser, password: EncryptedPassword): F[users.User] = ???
}
