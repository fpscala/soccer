package uz.soccer.utils.ciris

import _root_.ciris.ConfigDecoder
import uz.soccer.utils.derevo.Derive

object configDecoder extends Derive[Decoder.Id]

object Decoder {
  type Id[A] = ConfigDecoder[String, A]
}
