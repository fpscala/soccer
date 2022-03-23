package uz.soccer.config
import ciris.Secret
import uz.soccer.types.{JwtAccessTokenKeyConfig, PasswordSalt, TokenExpiration}

case class AppConfig(
  adminJwtConfig: AdminJwtConfig,
  tokenConfig: Secret[JwtAccessTokenKeyConfig],
  passwordSalt: Secret[PasswordSalt],
  tokenExpiration: TokenExpiration,
  dbConfig: DBConfig,
  redis: RedisConfig,
  serverConfig: HttpServerConfig,
  logConfig: LogConfig
)
