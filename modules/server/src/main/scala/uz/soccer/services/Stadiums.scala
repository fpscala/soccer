package uz.soccer.services

import cats.effect.{Resource, Sync}
import cats.syntax.all._
import skunk._
import skunk.implicits._
import uz.soccer.domain.Stadium.CreateStadium
import uz.soccer.domain.types.StadiumId
import uz.soccer.domain.{ID, Stadium}
import uz.soccer.effects.GenUUID
import uz.soccer.services.sql.StadiumSql

trait Stadiums[F[_]] {
  def create(stadium: CreateStadium): F[Stadium]

  def update(stadium: Stadium): F[Unit]

  def getAll: F[List[Stadium]]

  def delete(uuid: StadiumId): F[Unit]
}

object Stadiums {
  def apply[F[_]: Sync: GenUUID](implicit session: Resource[F, Session[F]]): Stadiums[F] = new Stadiums[F]
    with SkunkHelper[F] {
    override def create(stadium: CreateStadium): F[Stadium] =
      ID.make[F, StadiumId]
        .flatMap { id =>
          prepQueryUnique(StadiumSql.insert, id ~ stadium)
        }

    override def update(stadium: Stadium): F[Unit] =
      prepCmd(StadiumSql.update, stadium)

    override def getAll: F[List[Stadium]] =
      prepQueryAll(StadiumSql.selectAll)

    override def delete(uuid: StadiumId): F[Unit] =
      prepCmd(StadiumSql.delete, uuid)
  }
}
