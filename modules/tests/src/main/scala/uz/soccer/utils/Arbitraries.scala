package uz.soccer.utils
import org.scalacheck.Gen._
import org.scalacheck.{Arbitrary, Gen}
import uz.soccer.domain.custom.refinements.{EmailAddress, Password}
import uz.soccer.domain.{Gender, Role}
import uz.soccer.utils.Generators.nonEmptyStringGen

object Arbitraries {

  implicit lazy val arbGender: Arbitrary[Gender] = Arbitrary(oneOf(Gender.genders))
  implicit lazy val arbRole: Arbitrary[Role]     = Arbitrary(oneOf(Role.roles))
  implicit lazy val arbEmail: Arbitrary[EmailAddress] = Arbitrary(
    for {
      s0 <- nonEmptyStringGen(4, 8)
      s1 <- nonEmptyStringGen(3, 5)
      s2 <- nonEmptyStringGen(2, 3)
    } yield EmailAddress.unsafeFrom(s"$s0@$s1.$s2")
  )

  implicit lazy val arbPassword: Arbitrary[Password] = Arbitrary(
    for {
      s0 <- alphaUpperChar
      s1 <- nonEmptyStringGen(5, 8)
      s2 <- numChar
      s3 <- oneOf("!@#$%^&*")
    } yield Password.unsafeFrom(s"$s0$s1$s2$s3")
  )
}
