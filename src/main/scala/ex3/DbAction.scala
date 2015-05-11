package ex3

import java.sql.Connection


object DbAction {

  type DbAction[A] = Reader[Connection, A]

  def apply[A](a: A): DbAction[A] = new DbAction[A] {
    def apply(c: Connection) = a
  }

  def apply[A](f: Connection => A): DbAction[A] = new DbAction[A] {
    def apply(c: Connection) = f(c)
  }
}

