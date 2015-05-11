package ex2

import java.sql.Connection

trait DbAction[A] extends (Connection => A) {
  thisAction =>

  def map[B](f: A => B): DbAction[B] = DbAction { c => f(thisAction(c)) }

  def flatMap[B](f: A => DbAction[B]): DbAction[B] = DbAction { c => f(thisAction(c))(c) }

  def andThen[B](b: DbAction[B]): DbAction[B] = flatMap(_ => b)

  def >>[B](b: DbAction[B]): DbAction[B] = andThen(b)
}

object DbAction {
  def apply[A](a: A): DbAction[A] = new DbAction[A] {
    def apply(c: Connection) = a
  }

  def apply[A](f: Connection => A): DbAction[A] = new DbAction[A] {
    def apply(c: Connection) = f(c)
  }
}

