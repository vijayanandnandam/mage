package models

/**
  * Created by Fincash on 24-01-2017.
  *
  */

import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONLong, BSONObjectID, BSONString}
import java.util.Date

import models.JsonFormats._
import play.api.libs.json.JsObject

import scala.collection.mutable.ListBuffer

case class Contact(email: Option[String], mob: Option[String], tel: Option[String], fax: Option[String])
case class Address(amtid: Option[String], addressType: Option[String], address1: Option[String], address2: Option[String], pin: Option[String], city: Option[String],
                   district: Option[String], state: Option[String], country: Option[String], landmark: Option[String])
case class Nominee(ndtid: Option[String], nomineeName: Option[String], nomineeRelation: Option[String], nomineeDob: Option[String], nomineeAddress: Option[Address])
case class UserBasic(firstName: Option[String], middleName: Option[String], lastName: Option[String], panName: Option[String],
                     pan: Option[String], fatherName: Option[String], motherName: Option[String], contact: Option[Contact],
                     dob: Option[String], gender: Option[String], maritalStatus: Option[String])

case class UserAddress(permanentAddress: Option[Address], currentAddress: Option[Address], permanentEqualsCurrent: Boolean)

case class UserBank(buaid: Option[String], holderName: Option[String], accountType: Option[String], bank: Option[Bank], account: Option[Account], accountNumber: Option[String]);
case class Account(accountNumber: String, confirmAccountNumber: String)

case class UserFatca(futid: Option[String], income: Option[String], occupation: Option[String], sourceOfWealth: Option[String], nationality: Option[String], birthCountry: Option[String],
                     birthCity: Option[String], isIndianTaxPayer: Option[String], taxCountry: Option[String], tin: Option[String], politicallyExposed: Option[String], politicallyRelated: Option[String])

case class UserA(userBasic: UserBasic, userAddress: UserAddress, userBank: UserBank, userFatca: UserFatca)

case class EKycApiData(aadhar: Option[String], kycStatus: Option[String], responseCode: Option[String], aadharName: Option[String], pekrn: Option[String],
                       maidenName: Option[String], aadharState: Option[String], aadharOccupation: Option[String], aadharAddressType: Option[String], userid: Option[String],
                       residentialStatus: Option[String], aadharNationality: Option[String])



object UserJsonFormats {
  import play.api.libs.json.Json
  import play.api.data._
  import play.api.data.Forms._
  import reactivemongo.bson.Macros
  import reactivemongo.play.json.BSONFormats.BSONObjectIDFormat
  import models.JsonFormats._

  implicit val accountFormat = Json.format[Account]
  implicit val contactFormat = Json.format[Contact]
  implicit val addressFormat = Json.format[Address]
  implicit val eKycApiDataFormat = Json.format[EKycApiData]
  implicit val nomineeFormat = Json.format[Nominee]
  implicit val userBasicFormat = Json.format[UserBasic]
  implicit val userAddressFormat = Json.format[UserAddress]
  implicit val userBankFormat = Json.format[UserBank]
  implicit val userFatcaFormat = Json.format[UserFatca]
  implicit val userFormat = Json.format[UserA]
}
