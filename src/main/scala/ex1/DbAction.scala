package ex1

import java.sql.Connection

case class DbAction[A](thisAction: Connection => A) {

  def apply(c: Connection) = thisAction(c)

  def map[B](f: A => B): DbAction[B] = {
    DbAction(c => f(thisAction(c)))
  }

  def flatMap[B](f: A => DbAction[B]): DbAction[B] = {
    DbAction(c => f(thisAction(c))(c))
  }

}

object DbAction {

  //return: allows us to introduce plain values in our db program
  def apply[A](a: A): DbAction[A] = DbAction(c => a)
}
