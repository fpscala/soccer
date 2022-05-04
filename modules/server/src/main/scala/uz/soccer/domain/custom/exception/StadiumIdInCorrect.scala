package uz.soccer.domain.custom.exception

import uz.soccer.domain.types.StadiumId

import scala.util.control.NoStackTrace

case class StadiumIdInCorrect(uuid: StadiumId) extends NoStackTrace
