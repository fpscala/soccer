package uz.soccer.services.sql

import skunk._
import skunk.implicits._
import uz.soccer.domain.Role
import uz.soccer.domain.auth.{CreateUser, EncryptedPassword, UserId}
import uz.soccer.domain.custom.refinements.EmailAddress
import uz.soccer.domain.auth.User

object UserSQL {
  private val Columns = userId ~ userName ~ email ~ gender ~ encPassword ~ role

  val encoder: Encoder[UserId ~ CreateUser ~ EncryptedPassword] =
    Columns.contramap { case i ~ u ~ p =>
      i ~ u.name ~ u.email ~ u.gender ~ p ~ Role.USER
    }
  val decoder: Decoder[User ~ EncryptedPassword] =
    Columns.map { case i ~ n ~ e ~ g ~ p ~ r =>
      User(i, n, e, g, r) ~ p
    }

  val selectUser: Query[EmailAddress, User ~ EncryptedPassword] =
    sql"""
        SELECT * FROM users
        WHERE email = $email
       """.query(decoder)

  val insertUser: Query[UserId ~ CreateUser ~ EncryptedPassword, User ~ EncryptedPassword] =
    sql"""
        INSERT INTO users
        VALUES ($encoder)
        """.query(decoder)

}
