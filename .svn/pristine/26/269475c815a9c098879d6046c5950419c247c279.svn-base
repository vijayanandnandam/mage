package models

import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONLong, BSONObjectID, BSONString}
import java.util.Date

import scala.collection.mutable.ListBuffer
import reactivemongo.bson.BSONObjectID

case class CNDRow(cndrfnum: String, cndname: String)
case class BMTRow(bmtrfnum: String, bmtbankname: String)
case class BankSuggestion(bmtrfnum: String, bmtbankname: String, bbtrfnum: String, bbtbranchname: Option[String], bbtifsccode: String)
case class Employee(_id: BSONObjectID, name: String, address: String, dob: Date,
                    joiningDate: Date, designation: String)
case class User(_id: BSONObjectID, username: String, email: String, password: String,  mob: String)
case class UserToken(token_id: Option[Int], token: String, end_time: Long, status: String, user_id: Int)
case class MailStatus(_id: BSONObjectID, mailId: String, from: String, to: String,
                      subject: String, body: String, sent: String = "Y", delivered: String = "N",
                      dropped: String = "N", bounce: String = "N", spam: String = "N",
                      unsub: String = "N", click: String = "N", open: String = "N")
case class MailAttachment(_id: BSONObjectID, mailId: String, fileName: String, path: String)
case class MailAck(mailId: String, event: String, body: String, _id: BSONObjectID)
case class Test(name: String, data: String)

case class Bank(bankName: Option[String], IFSC: Option[String], city: Option[String], state: Option[String], branch: Option[String], district: Option[String], MICR: Option[String], address: Option[String])
case class BankSearchResult(numFound: Long, banks: Seq[Bank])
case class BankSearchQuery(keyword: Option[String], branch: Option[String], city: Option[String], 
                            bank_name: Option[String], IFSC: Option[String], bank_id: Option[Int])

case class PinCode(office_name: String, pin: Int, office_type: String, delivery_status: String, division_name: String, region_name: String,
                   circle_name: String, taluk: String, district: String, state: String)
                   
case class CashFunds(fund_name: String, options: Seq[String], risk: String, age: Double , aum: Double, 
                    minInvestment: Double, return1yr: Double)

/**
  * Helper for pagination.
  */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object JsonFormats {
  import play.api.libs.json.Json
  import play.api.data._
  import play.api.data.Forms._
  import reactivemongo.bson.Macros
  import reactivemongo.play.json.BSONFormats.BSONObjectIDFormat

  implicit val cndRowFormat = Json.format[CNDRow]
  implicit val bmtRowFormat = Json.format[BMTRow]
  implicit val bankSuggestionFormat = Json.format[BankSuggestion]
  implicit val employeeFormat = Json.format[Employee]
  implicit val userFormat = Json.format[User]
  implicit val testFormat = Json.format[Test]
  implicit val userTokenFormat = Json.format[UserToken]
  implicit val mailStatusFormat = Json.format[MailStatus]
  implicit val mailStatusFormatBSON = Macros.handler[MailStatus]
  implicit val mailAttachmentFormat = Json.format[MailAttachment]
  implicit val mailAckFormat = Json.format[MailAck]
  implicit val pincodeFormat = Json.format[PinCode]
  implicit val bankFormat = Json.format[Bank]
  implicit val bankSearchResultFormat = Json.format[BankSearchResult]
  implicit val bankSearchQueryFormat = Json.format[BankSearchQuery]
  implicit val cashFundsFormat = Json.format[CashFunds]
}
