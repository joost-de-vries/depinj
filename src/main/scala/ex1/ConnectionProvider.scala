package ex1

import java.sql.DriverManager

//DbAction interpreter
abstract class ConnProvider {
  def run[A](f: DbAction[A]): A
}

object ConnProvider {
  //called mkProvider in the talk
  def apply(driver: String, url: String, user: String, pwd: String) = new ConnProvider {
    def run[A](dbAction: DbAction[A]): A = {
      Class.forName(driver)
      val conn = DriverManager.getConnection(url, user, pwd)
      try {
        dbAction(conn)
      }
      finally {
        conn.close
      }
    }
  }
}

