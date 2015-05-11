package shared

import java.sql.{Connection, DriverManager}

//DbAction interpreter
abstract class ConnProvider {
  def run[A](f: Connection => A): A
}

object ConnProvider {
  //called mkProvider in the talk
  def apply(driver: String, url: String, user: String, pwd: String) = new ConnProvider {
    def run[A](f: Connection => A): A = {
      Class.forName(driver)
      val conn = DriverManager.getConnection(url, user, pwd)
      try {
        val result = f(conn)
        conn.commit()
        result
      } finally {
        conn.close
      }
    }
  }

  lazy val SqliteTestDB = {
    val cp = ConnProvider("org.h2.Driver", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", user = "SA", pwd = "")
    cp.run { c =>
      c.prepareCall("create table USERS ( ID varchar(20), PWD varchar(20) );").execute()
      c.prepareCall("insert into USERS (ID,PWD)  values ('guestuser','typesafe');").execute()
    }
    cp
  }
  lazy val MysqlProdDB = ConnProvider("org.gjt.mm.mysql.Driver", "jdbc:mysql://prod:3306/", user = "one", pwd = "two")

}

