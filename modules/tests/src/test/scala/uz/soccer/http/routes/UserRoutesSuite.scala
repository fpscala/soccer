package uz.soccer.http.routes

import cats.effect.{IO, Sync}
import cats.implicits._
import eu.timepit.refined.auto.autoUnwrap
import org.http4s.Method.POST
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.implicits.http4sLiteralsSyntax
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt
import uz.soccer.domain.User
import uz.soccer.domain.User.{CreateUser, UserWithPassword}
import uz.soccer.domain.custom.refinements.{EmailAddress, Password}
import uz.soccer.services.Users
import uz.soccer.stub_services.{AuthMock, UsersStub}
import uz.soccer.utils.Generators._
import uz.soccer.utils.HttpSuite

object UserRoutesSuite extends HttpSuite {

  def users[F[_]: Sync](user: User, pass: Password): Users[F] = new UsersStub[F] {
    override def find(
      email: EmailAddress
    ): F[Option[UserWithPassword]] =
      if (user.email.equalsIgnoreCase(email))
        SCrypt.hashpw[F](pass).map { hash =>
          UserWithPassword(user, hash).some
        }
      else
        none[UserWithPassword].pure[F]

    override def create(
      userParam: CreateUser,
      password: PasswordHash[SCrypt]
    ): F[User] = user.pure[F]
  }

  test("POST create") {
    val gen = for {
      u <- userGen
      c <- createUserGen
      b <- booleanGen
    } yield (u, c, b)

    forall(gen) { case (user, newUser, conflict) =>
      for {
        auth <- AuthMock[IO](users(user, newUser.password))
        (postData, shouldReturn) =
          if (conflict)
            (newUser.copy(email = user.email), Status.Conflict)
          else
            (newUser, Status.Created)
        req    = POST(postData, uri"/auth/user")
        routes = UserRoutes[IO](auth).routes
        res <- expectHttpStatus(routes, req)(shouldReturn)
      } yield res
    }
  }
}
