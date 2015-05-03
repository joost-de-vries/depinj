package ex1

import java.sql.Connection

/* an action on the database. obviously requires a connection to evaluate */
case class DbAction[A](thisAction: Connection => A) {

  //evaluate the action
  def apply(c: Connection) = thisAction(c)

  //transform the result of this action
  def map[B](f: A => B): DbAction[B] = DbAction(c => f(thisAction(c)))

  // map : (A => B) => (DB[A] => DB[B])

  //return a composite action of this action and the next one that uses the result of this action
  def flatMap[B](f: A => DbAction[B]): DbAction[B] = DbAction(c =>
    f(thisAction(c))(c)
  )

  // flatMap : (A => DB[B]) => (DB[A] => DB[B])

}

object DbAction {
  //return: allows us to introduce plain values in our db program
  def apply[A](a: A): DbAction[A] = DbAction(c => a)
}

