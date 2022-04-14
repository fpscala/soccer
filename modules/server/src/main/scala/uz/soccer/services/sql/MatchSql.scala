package uz.soccer.services.sql

import skunk._
import skunk.codec.all.timestamp
import skunk.implicits._
import uz.soccer.domain.Match
import uz.soccer.domain.Match.CreateMatch
import uz.soccer.domain.types.MatchId

object MatchSql {
  val matchId: Codec[MatchId] = identity[MatchId]

  private val Columns = matchId ~ timestamp ~ timestamp ~ StadiumSql.stadiumId

  val encoder: Encoder[MatchId ~ CreateMatch] =
    Columns.contramap { case i ~ m =>
      i ~ m.startTime ~ m.endTime ~ m.stadiumId
    }

  val decoder: Decoder[Match] =
    Columns.map { case i ~ s ~ e ~ sid =>
      Match(i, s, e, sid)
    }

  val insert: Query[MatchId ~ CreateMatch, Match] =
    sql"""INSERT INTO matches VALUES ($encoder) returning *""".query(decoder)

  val selectAll: Query[Void, Match] =
    sql"""SELECT * FROM matches""".query(decoder)

  val update: Command[Match] =
    sql"""UPDATE matches SET
           start_time = $timestamp,
           end_time = $timestamp,
           stadium_id = ${StadiumSql.stadiumId} WHERE uuid = $matchId
       """.command.contramap(m => m.startTime ~ m.endTime ~ m.stadiumId ~ m.uuid)

  val delete: Command[MatchId] =
    sql"""DELETE FROM matches WHERE uuid = $matchId""".command
}
