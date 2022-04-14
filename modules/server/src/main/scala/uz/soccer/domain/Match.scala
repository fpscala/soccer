package uz.soccer.domain

import derevo.cats.show
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import uz.soccer.domain.types.{MatchId, StadiumId}
import java.time.LocalDateTime

@derive(decoder, encoder, show)
case class Match(uuid: MatchId, startTime: LocalDateTime, endTime: LocalDateTime, stadiumId: StadiumId)

object Match {
  @derive(decoder, encoder, show)
  case class CreateMatch(startTime: LocalDateTime, endTime: LocalDateTime, stadiumId: StadiumId)
}
