package uz.soccer.services

import cats.effect.{Resource, Sync}
import cats.syntax.all._
import skunk._
import skunk.implicits._
import uz.soccer.domain.Match.CreateMatch
import uz.soccer.domain.custom.exception.{DateTimeInCorrect, StadiumIdInCorrect}
import uz.soccer.domain.types.MatchId
import uz.soccer.domain.{ID, Match}
import uz.soccer.effects.GenUUID
import uz.soccer.services.sql.MatchSql

trait Matches[F[_]] {
  def create(`match`: CreateMatch): F[Match]

  def update(`match`: Match): F[Unit]

  def getAll: F[List[Match]]

  def delete(uuid: MatchId): F[Unit]
}

object Matches {
  def apply[F[_]: Sync: GenUUID](implicit session: Resource[F, Session[F]]): Matches[F] = new Matches[F]
    with SkunkHelper[F] {
    override def create(`match`: CreateMatch): F[Match] =
      ID.make[F, MatchId]
        .flatMap { id =>
          prepQueryUnique(MatchSql.insert, id ~ `match`)
        }
        .recoverWith {
          case SqlState.ForeignKeyViolation(_) =>
            StadiumIdInCorrect(`match`.stadiumId).raiseError[F, Match]
          case SqlState.DatetimeFieldOverflow(_) =>
            DateTimeInCorrect(`match`.startTime, `match`.endTime).raiseError[F, Match]
        }

    override def update(`match`: Match): F[Unit] =
      prepCmd(MatchSql.update, `match`)

    override def getAll: F[List[Match]] =
      prepQueryAll(MatchSql.selectAll)

    override def delete(uuid: MatchId): F[Unit] =
      prepCmd(MatchSql.delete, uuid)
  }
}
