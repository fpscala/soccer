package uz.soccer.services.sql

import skunk._
import skunk.implicits._
import uz.soccer.domain.types.{StadiumId, TeamId, TeamName, UserId}
import uz.soccer.domain.{Stadium, Team}

object TeamSql {
  val teamId: Codec[TeamId] = identity[TeamId]

  private val Columns = teamId ~ teamName

  val encoder: Encoder[TeamId ~ TeamName] =
    Columns.contramap { case i ~ n =>
      i ~ n
    }

  val decoder: Decoder[Team] =
    Columns.map { case i ~ n =>
      Team(i, n)
    }

  val insert: Query[TeamId ~ TeamName, Team] =
    sql"""INSERT INTO teams VALUES ($encoder) returning *""".query(decoder)

  val selectAll: Query[Void, Team] =
    sql"""SELECT * FROM teams""".query(decoder)

  val selectByUserId: Query[UserId, Team] =
    sql"""SELECT t.* FROM player_teams pt
          INNER JOIN teams t on t.uuid = pt.team_id
          WHERE pt.player_id = ${UserSQL.userId}""".query(decoder)

  val update: Command[Team] =
    sql"""UPDATE teams SET name = $teamName WHERE uuid = $teamId
       """.command.contramap(t => t.name ~ t.uuid)

  val delete: Command[TeamId] =
    sql"""DELETE FROM teams WHERE uuid = $teamId""".command
}
