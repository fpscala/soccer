package uz.soccer.utils

import cats.data.OptionT
import cats.effect.IO
import dev.profunktor.auth.JwtAuthMiddleware
import dev.profunktor.auth.jwt.{JwtAuth, JwtToken}
import eu.timepit.refined.auto.autoUnwrap
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.server.AuthMiddleware
import pdi.jwt.{JwtAlgorithm, JwtClaim}
import uz.soccer.config.jwtConfig
import uz.soccer.domain.User
import uz.soccer.implicits.CirceDecoderOps
import uz.soccer.services.redis.RedisClient
import uz.soccer.stub_services.RedisClientMock
import weaver.scalacheck.Checkers
import weaver.{Expectations, SimpleIOSuite}

trait HttpSuite extends SimpleIOSuite with Checkers {

  val RedisClient: RedisClient[IO] = RedisClientMock[IO]

  def findUser(token: JwtToken): JwtClaim => F[Option[User]] = _ =>
    OptionT(RedisClient.get(token.value))
      .map(_.as[User])
      .value

  protected val usersMiddleware: AuthMiddleware[F, User] =
    JwtAuthMiddleware[F, User](JwtAuth.hmac(jwtConfig.tokenConfig.value.secret, JwtAlgorithm.HS256), findUser)

  def expectHttpBodyAndStatus[A: Encoder](routes: HttpRoutes[IO], req: Request[IO])(
    expectedBody: A,
    expectedStatus: Status
  ): IO[Expectations] =
    routes.run(req).value.flatMap {
      case Some(resp) =>
        resp.asJson.map { json =>
          expect.all(resp.status == expectedStatus, json.dropNullValues == expectedBody.asJson.dropNullValues)
        }
      case None => IO.pure(failure("route not found"))
    }

  def expectHttpStatus(routes: HttpRoutes[IO], req: Request[IO])(expectedStatus: Status): IO[Expectations] =
    routes.run(req).value.map {
      case Some(resp) => expect.same(resp.status, expectedStatus)
      case None       => failure("route not found")
    }

  def expectHttpFailure(routes: HttpRoutes[IO], req: Request[IO]): IO[Expectations] =
    routes.run(req).value.attempt.map {
      case Left(_)  => success
      case Right(_) => failure("expected a failure")
    }

}
