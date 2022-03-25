package uz.soccer.http.routes

import cats.effect.IO
import cats.implicits._
import eu.timepit.refined.types.string.NonEmptyString
import org.http4s.Method.POST
import org.http4s.Status
import org.http4s.client.dsl.io._
import org.http4s.implicits.http4sLiteralsSyntax
import uz.soccer.config.jwtConfig
import uz.soccer.domain.auth._
import uz.soccer.http.auth.users.{User, UserWithPassword}
import uz.soccer.security.Crypto
import uz.soccer.services.Users
import uz.soccer.stub_services.{AuthMock, UsersStub}
import uz.soccer.utils.Generators.{booleanGen, createUserGen, userGen}
import uz.soccer.utils.HttpSuite

object UserRoutesSuite extends HttpSuite {

  def users(user: User, pass: Password, crypto: Crypto): Users[F] = new UsersStub[F] {
    override def find(
      username: UserName
    ): F[Option[UserWithPassword]] =
      if (user.name.value.equalsIgnoreCase(username.value))
        Option(UserWithPassword(user.id, user.name, crypto.encrypt(pass))).pure[F]
      else
        none[UserWithPassword].pure[F]

    override def create(
      username: UserName,
      password: EncryptedPassword
    ): F[UserId] = user.id.pure[F]
  }

  test("POST create") {
    val gen = for {
      u <- userGen
      c <- createUserGen
      b <- booleanGen
    } yield (u, c, b)

    forall(gen) { case (user, newUser, conflict) =>
      for {
        crypto <- Crypto[IO](jwtConfig.passwordSalt.value)
        auth   <- AuthMock[IO](users(user, newUser.password.toDomain, crypto), crypto)
        (postData, shouldReturn) =
          if (conflict)
            (newUser.copy(username = UserNameParam(NonEmptyString.unsafeFrom(user.name.value))), Status.Conflict)
          else
            (newUser, Status.Created)
        req    = POST(postData, uri"/auth/user")
        routes = UserRoutes[IO](auth).routes
        res <- expectHttpStatus(routes, req)(shouldReturn)
      } yield res
    }
  }
}
