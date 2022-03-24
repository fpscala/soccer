package uz.soccer.http.routes

import cats.effect.IO
import cats.implicits._
import eu.timepit.refined.types.string.NonEmptyString
import org.http4s.Method.POST
import org.http4s.Status
import org.http4s.client.dsl.io._
import org.http4s.implicits.http4sLiteralsSyntax
import uz.soccer.config.{JwtConfig, jwtConfig}
import uz.soccer.domain.auth.{Password, UserName, UserNameParam}
import uz.soccer.http.auth.users.{User, UserWithPassword}
import uz.soccer.security.{Crypto, JwtExpire, Tokens}
import uz.soccer.services.{Auth, Users}
import uz.soccer.stub_services.UsersStub
import uz.soccer.utils.Generators.{booleanGen, userCredentialGen, userGen}
import uz.soccer.utils.HttpSuite

object LoginRoutesSuite extends HttpSuite {

  def auth(config: JwtConfig, user: User, pass: Password): IO[Auth[IO]] =
    for {
      tokens <- JwtExpire.make[IO].map(Tokens.make[IO](_, config.tokenConfig.value, config.tokenExpiration))
      crypto <- Crypto.make[IO](config.passwordSalt.value)
      userWithPassword = UserWithPassword(user.id, UserName(user.name.value.toLowerCase), crypto.encrypt(pass))

      auth = Auth.make[IO](config.tokenExpiration, tokens, users(userWithPassword), RedisMock, crypto)
    } yield auth

  def users(user: UserWithPassword): Users[IO] = new UsersStub[IO] {
    override def find(
      username: UserName
    ): IO[Option[UserWithPassword]] =
      (
        if (user.name.value == username.value)
          Option(user)
        else
          none[UserWithPassword]
      ).pure[IO]
  }

  test("POST login") {
    val gen = for {
      u <- userGen
      c <- userCredentialGen
      b <- booleanGen
    } yield (u, c, b)
    forall(gen) { case (user, c, isCorrect) =>
      auth(jwtConfig, user, c.password.toDomain).flatMap { auth =>
        val (postData, shouldReturn) =
          if (isCorrect)
            (c.copy(username = UserNameParam(NonEmptyString.unsafeFrom(user.name.value))), Status.Ok)
          else
            (c, Status.Forbidden)
        val req    = POST(postData, uri"/auth/login")
        val routes = LoginRoutes[IO](auth).routes
        expectHttpStatus(routes, req)(shouldReturn)
      }
    }
  }
}
