package uz.soccer.config
import ciris.Secret
import uz.soccer.types.{AdminUserTokenConfig, JwtClaimConfig, JwtSecretKeyConfig}

case class AdminJwtConfig(
  secretKey: Secret[JwtSecretKeyConfig],
  claimStr: Secret[JwtClaimConfig],
  adminToken: Secret[AdminUserTokenConfig]
)
