package ex1
import java.sql.Connection
import java.sql.DriverManager
package dbaction {
  /* an action on the database. obviously requires a connection to evaluate */
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
    //useful if we want a nop dbaction that doesn't really use the connection
    def apply[A](a: A): DbAction[A] = DbAction(c => a)
  }
  //DbAction interpreter
  abstract class ConnProvider {
    def apply[A](f: DbAction[A]): A
  }
  object ConnProvider {
    //called mkProvider in the talk
    def apply(driver: String, url: String, user: String, pwd: String) = new ConnProvider {
      def apply[A](f: DbAction[A]): A = {
        Class.forName(driver)
        val conn = DriverManager.getConnection(url, user, pwd)
        try { f(conn) }
        finally { conn.close }
      }
    }
  }
}
object Main extends App {
  import dbaction._

  //disabling the implicit magic to make the code more explicit
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

  /* a composed dbaction. not yet evaluated */
  def changePwd(userid: String, oldPwd: String, newPwd: String): DbAction[Boolean] = {
    val getPwdAction: DbAction[String] = DbAction(getUserPwd(userid))

    //transform the DbAction[Unit] to a DbAction[Boolean]
    //which is the required result
    val setPwdAction: DbAction[Boolean] = for {
      _ <- DbAction(setUserPwd(userid, newPwd))  //uses map
    } yield true

    val nopAction = DbAction(false)
    
    //compose the two
    for {
      pwd <- getPwdAction	    //uses flatmap
      eq <- if (pwd == oldPwd)  //uses map
        setPwdAction
      else
        nopAction
    } yield eq
  }
  
  /* the original function from the talk */
  def changePwdOrig(userid: String, oldPwd: String, newPwd: String): DbAction[Boolean] = 
    for {
    pwd <- DbAction(getUserPwd(userid))
    eq <- if (pwd == oldPwd)
      for {
        _ <- DbAction(setUserPwd(userid, newPwd))
      } yield true
    else
      DbAction(false)
  } yield eq

  lazy val sqliteTestDB = ConnProvider("org.h2.Driver", "jdbc:h2:./target/test", user = "SA", pwd = "")
  lazy val mysqlProdDB = ConnProvider("org.gjt.mm.mysql.Driver", "jdbc:mysql://prod:3306/", user = "one", pwd = "two")

  def myProgram(userid: String): ConnProvider => Unit =
    connProvider => {
      println("Enter old password")
      val oldPwd = readLine
      println("Enter new password")
      val newPwd = readLine
      connProvider(changePwd(userid, oldPwd, newPwd)) //evaluate composed dbaction
    }
    
  def runInTest[A](f: ConnProvider => A): A = f(sqliteTestDB)  //the 'dependency injection'
  def runInProduction[A](f: ConnProvider => A): A = f(mysqlProdDB)
  
  override def main(args: Array[String]) = runInTest(myProgram("guestuser"))
}