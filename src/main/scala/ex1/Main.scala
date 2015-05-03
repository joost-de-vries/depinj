package ex1

import ex1.PwdMgmt._

import scala.io.StdIn.readLine

object Main extends App {


  lazy val sqliteTestDB = ConnProvider("org.h2.Driver", "jdbc:h2:./target/test", user = "SA", pwd = "")
  lazy val mysqlProdDB = ConnProvider("org.gjt.mm.mysql.Driver", "jdbc:mysql://prod:3306/", user = "one", pwd = "two")

  def myProgram(userid: String): ConnProvider => Unit =
    connProvider => {
      println("Enter old password")
      val oldPwd = readLine()
      println("Enter new password")
      val newPwd = readLine()
      connProvider.run(changePwd(userid, oldPwd, newPwd)) //evaluate composed dbaction
    }

  def runInTest[A](dbProgram: ConnProvider => A): A = dbProgram(sqliteTestDB)

  def runInProduction[A](dbProgram: ConnProvider => A): A = dbProgram(mysqlProdDB)

  runInTest(myProgram("guestuser"))
}