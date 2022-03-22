package uz.soccer.db.algebras

case class Algebras[F[_]](
  user: UserAlgebra[F]
)
