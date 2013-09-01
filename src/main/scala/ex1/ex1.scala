package ex1
import java.sql.Connection
import java.sql.DriverManager
package dbaction{
  /* an action on the database that obviously requires a connection */
  case class DbAction[A](g: Connection => A) {
    //evaluate the action
    def apply(c: Connection) = g(c)
    //for promoting existing functions
    def map[B](f: A => B): DbAction[B] = DbAction(c => f(g(c)))
    // map : (A => B) => (DB[A] => DB[B])
    //for combining 2 dbactions
    def flatMap[B](f: A => DbAction[B]): DbAction[B] = DbAction(c => f(g(c))(c))
    // flatMap : (A => DB[B]) => (DB[A] => DB[B])
  }
  object DbAction {
    //called pure in the talk
    def apply[A](a: A): DbAction[A] = DbAction(c => a)
  }
  //DbAction interpreter
  abstract class ConnProvider {
    def apply[A](f: DbAction[A]): A
  }
  object ConnProvider{
    //called mkProvider in the talk
  def apply(driver: String, url: String) = new ConnProvider {
    def apply[A](f: DbAction[A]): A = {
      Class.forName(driver)
      val conn = DriverManager.getConnection(url)
      try { f(conn) }
      finally { conn.close }
    }
  }
  }
}
object Main extends App {
  import dbaction._
  //  implicit def dbaction[A](f:Connection => A) =DbAction(f)
  def setUserPwd(id: String, pwd: String): Connection => Unit =
    c => {
      val stmt = c.prepareStatement("update users set pwd = ? where id = ?")
      stmt.setString(1, pwd)
      stmt.setString(2, id)
      stmt.executeUpdate
      stmt.close
    }
  def getUserPwd(id: String): Connection => String =
    c => {
      val stmt = c.prepareStatement("select pwd from users where id = ?")
      try {
        stmt.setString(1, id)
        stmt.executeQuery().getString(1)
      } finally {
        stmt.close
      }
    }
  def changePwd(userid: String, oldPwd: String, newPwd: String): DbAction[Boolean] =
    for {
      pwd <- DbAction(getUserPwd(userid))
      eq <- if (pwd == oldPwd)
        for {
          _ <- DbAction(setUserPwd(userid, newPwd))
        } yield true
      else
        DbAction(false)
    } yield eq

  lazy val sqliteTestDB = ConnProvider("org.h2.Driver", "jdbc:h2:~/test?user=SA&password=")
  lazy val mysqlProdDB = ConnProvider("org.gjt.mm.mysql.Driver", "jdbc:mysql://prod:3306/?user=one&password=two")

  def myProgram(userid: String): ConnProvider => Unit =
    r => {
      println("Enter old password")
      val oldPwd = readLine
      println("Enter new password")
      val newPwd = readLine
      r(changePwd(userid, oldPwd, newPwd))
    }
  def runInTest[A](f: ConnProvider => A): A = f(sqliteTestDB)
  def runInProduction[A](f: ConnProvider => A): A = f(mysqlProdDB)
  override def main(args: Array[String]) = runInTest(myProgram("guestuser"))
}