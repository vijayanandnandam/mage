package models

case class ErrorModel(errorCode:String,errorMessage:String)

object ErrorModelFormat{
  
  import play.api.libs.json.Json
  
  implicit val errorModelFormat = Json.format[ErrorModel]
}