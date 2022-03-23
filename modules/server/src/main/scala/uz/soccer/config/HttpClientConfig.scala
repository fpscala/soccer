package uz.soccer.config

import scala.concurrent.duration.FiniteDuration

case class HttpClientConfig(
    timeout: FiniteDuration,
    idleTimeInPool: FiniteDuration
)