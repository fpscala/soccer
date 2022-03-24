package uz.soccer.config

import ciris.Secret
import uz.soccer.types.{JwtAccessTokenKeyConfig, PasswordSalt, TokenExpiration}

case class JwtConfig(
  tokenConfig: Secret[JwtAccessTokenKeyConfig],
  passwordSalt: Secret[PasswordSalt],
  tokenExpiration: TokenExpiration
)
