package uz.soccer.services.sql

import skunk._
import skunk.implicits._
import uz.soccer.domain.types.{TeamId, UserId}

object PlayerTeamsSql {
  private val Columns = UserSQL.userId ~ TeamSql.teamId

  val encoder: Encoder[UserId ~ TeamId] =
    Columns.contramap { case userId ~ teamId =>
      userId ~ teamId
    }

  val insert: Command[UserId ~ TeamId] =
    sql"""INSERT INTO player_teams VALUES ($encoder)""".command

  val delete: Command[UserId ~ TeamId] =
    sql"""DELETE FROM player_teams WHERE player_id = ${UserSQL.userId} AND team_id = ${TeamSql.teamId}""".command
}
