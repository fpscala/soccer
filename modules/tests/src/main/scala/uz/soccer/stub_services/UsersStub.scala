package uz.soccer.stub_services

import uz.soccer.domain.auth.{EncryptedPassword, UserId, UserName}
import uz.soccer.http.auth.users.UserWithPassword
import uz.soccer.services.Users

class UsersStub[F[_]] extends Users[F] {
  def find(username: UserName): F[Option[UserWithPassword]] = ???

  def create(username: UserName, password: EncryptedPassword): F[UserId] = ???
}
