package models

/**
  * Created by Rashmi on 19-03-2017.
  */

/*
@Pawan


NOTE

do not change name of case class variables as they are being used as stated below in frontend application as this is related to userObject

        DO NOT CHANGE NAME  OF THE VARIALES CAN RESULT IN UNEXPECTED ERRORS AT FRONTEND


*/
case class UserLoginObject(username:Option[String],userid:Option[Long], foliocount:Option[Long], firstname : Option[String],pan : Option[String], mob : Option[String])

object UserLoginJsonFormats {
  import play.api.libs.json._

  implicit val userLoginFormat =  Json.format[UserLoginObject]
}