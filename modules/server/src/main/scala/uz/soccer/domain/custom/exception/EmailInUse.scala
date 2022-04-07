package uz.soccer.domain.custom.exception

import uz.soccer.domain.custom.refinements.EmailAddress
import scala.util.control.NoStackTrace

case class EmailInUse(email: EmailAddress) extends NoStackTrace
