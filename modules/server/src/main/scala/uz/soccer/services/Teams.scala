package uz.soccer.services

import cats.effect.{Resource, Sync}
import cats.syntax.all._
import skunk._
import skunk.implicits._
import uz.soccer.domain.custom.exception.TeamNameInUse
import uz.soccer.domain.types.{TeamId, TeamName, UserId}
import uz.soccer.domain.{ID, Team}
import uz.soccer.effects.GenUUID
import uz.soccer.services.sql.TeamSql

trait Teams[F[_]] {
  def create(teamName: TeamName): F[Team]

  def update(team: Team): F[Unit]

  def getAll: F[List[Team]]

  def getByUserId(uuid: UserId): F[List[Team]]

  def delete(uuid: TeamId): F[Unit]
}

object Teams {
  def apply[F[_]: Sync: GenUUID](implicit session: Resource[F, Session[F]]): Teams[F] = new Teams[F]
    with SkunkHelper[F] {
    override def create(name: TeamName): F[Team] =
      ID.make[F, TeamId]
        .flatMap { id =>
          prepQueryUnique(TeamSql.insert, id ~ name)
        }
        .recoverWith { case SqlState.UniqueViolation(_) =>
          TeamNameInUse(name).raiseError[F, Team]
        }

    override def update(team: Team): F[Unit] =
      prepCmd(TeamSql.update, team)

    override def getByUserId(uuid: UserId): F[List[Team]] =
      prepQueryList(TeamSql.selectByUserId, uuid)

    override def getAll: F[List[Team]] =
      prepQueryAll(TeamSql.selectAll)

    override def delete(uuid: TeamId): F[Unit] =
      prepCmd(TeamSql.delete, uuid)
  }
}
