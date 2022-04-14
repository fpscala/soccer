package uz.soccer.domain.custom.exception

import uz.soccer.domain.types.TeamName

import scala.util.control.NoStackTrace

case class TeamNameInUse(name: TeamName) extends NoStackTrace
