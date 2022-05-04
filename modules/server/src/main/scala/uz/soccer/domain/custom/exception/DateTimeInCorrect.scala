package uz.soccer.domain.custom.exception

import uz.soccer.domain.types.StadiumId

import java.time.LocalDateTime
import scala.util.control.NoStackTrace

case class DateTimeInCorrect(times: LocalDateTime*) extends NoStackTrace
