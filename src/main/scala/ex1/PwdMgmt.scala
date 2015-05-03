package ex1

import java.sql.Connection


object PwdMgmt {

  //disabling the implicit magic to make the code more explicit
  //  implicit def dbaction[A](f:Connection => A) =DbAction(f)
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

  /* a composed dbaction. not yet evaluated */
  def changePwd(userid: String, oldPwd: String, newPwd: String): DbAction[Boolean] = {
    val getPwdAction: DbAction[String] = DbAction(getUserPwd(userid))

    //transform the DbAction[Unit] to a DbAction[Boolean]
    //which is the required result
    val setPwdAction: DbAction[Boolean] = for {
      _ <- DbAction(setUserPwd(userid, newPwd))
    } yield true

    val nopAction = DbAction(false)

    //compose the two
    for {
      pwd <- getPwdAction //uses flatmap
      eq <- if (pwd == oldPwd) //uses map
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

}
