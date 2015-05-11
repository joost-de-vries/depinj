package ex3


trait Reader[C, A] extends (C => A) {
  thisAction =>

  def map[B](f: A => B): Reader[C, B] = Reader { c => f(thisAction(c)) }

  def flatMap[B](f: A => Reader[C, B]): Reader[C, B] = Reader { c => f(thisAction(c))(c) }

}

object Reader {

  import scala.io.StdIn._

  def apply[C, A](f: C => A): Reader[C, A] = new Reader[C, A] {
    def apply(c: C) = f(c)
  }

  def program() = {
    println("your name?");
    val name = readLine();
    println(s"hello $name");
  }
}

