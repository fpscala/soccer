package uz.soccer.http.routes

import cats.effect.{IO, Sync}
import cats.implicits._
import eu.timepit.refined.auto.autoUnwrap
import org.http4s.Method.POST
import org.http4s.Status
import org.http4s.client.dsl.io._
import org.http4s.implicits.http4sLiteralsSyntax
import tsec.passwordhashers.jca.SCrypt
import uz.soccer.domain.User
import uz.soccer.domain.User.UserWithPassword
import uz.soccer.domain.custom.refinements.{EmailAddress, Password}
import uz.soccer.services.Users
import uz.soccer.stub_services.{AuthMock, UsersStub}
import uz.soccer.utils.Generators.{booleanGen, userCredentialGen, userGen}
import uz.soccer.utils.HttpSuite

object LoginRoutesSuite extends HttpSuite {
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
  }

  test("POST login") {
    val gen = for {
      u <- userGen
      c <- userCredentialGen
      b <- booleanGen
    } yield (u, c, b)

    forall(gen) { case (user, c, isCorrect) =>
      for {
        auth <- AuthMock[IO](users(user, c.password))
        (postData, shouldReturn) =
          if (isCorrect)
            (c.copy(email = user.email), Status.Ok)
          else
            (c, Status.Forbidden)
        req    = POST(postData, uri"/auth/login")
        routes = LoginRoutes[IO](auth).routes
        res <- expectHttpStatus(routes, req)(shouldReturn)
      } yield res
    }
  }
}
