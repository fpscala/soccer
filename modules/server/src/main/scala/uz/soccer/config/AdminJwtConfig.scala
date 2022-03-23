package uz.soccer.config

import ciris.Secret
import uz.soccer.types.{AdminUserTokenConfig, JwtSecretKeyConfig}

case class AdminJwtConfig(
  secretKey: Secret[JwtSecretKeyConfig],
  adminToken: Secret[AdminUserTokenConfig]
)
