package ex1

import java.sql.Connection


object PwdMgmt {

  def setUserPwd(id: String, pwd: String): Connection => Unit =
    c => {
      val stmt = c.prepareStatement("update users set pwd = ? where id = ?")
      stmt.setString(1, pwd)
      stmt.setString(2, id)
      stmt.executeUpdate
      stmt.close()
    }

  def getUserPwd(id: String): Connection => String =
    c => {
      val stmt = c.prepareStatement("select pwd from users where id = ?")
      try {
        stmt.setString(1, id)
        stmt.executeQuery().getString(1)
      } finally {
        stmt.close()
      }
    }

  def changePwd(userid: String, oldPwd: String, newPwd: String): DbAction[Boolean] = {
    val getPwdAction: DbAction[String] = DbAction(getUserPwd(userid))

    val setPwdAction: DbAction[Boolean] = for {
      _ <- DbAction(setUserPwd(userid, newPwd))
    } yield true

    //compose the two
    for {
      pwd <- getPwdAction //uses flatmap
      eq <- if (pwd == oldPwd) //uses map
        setPwdAction
      else
        DbAction(false)
    } yield eq
  }
}
