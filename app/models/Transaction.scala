package models

import play.api.libs.json.Json
import models.FundsJsonFormats._
import reactivemongo.play.json._
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future
import java.util.Date

import models.integration.enumerations.BuySellEnum.BuySellEnum

import scala.collection.mutable.ListBuffer

case class Transaction(folioNo: String, FundId: Long, fundName: String, plan: String, divFreq: String, divOption: String, transactionList: ListBuffer[TransactionDetails])

case class TransactionDetails(transDate: Date, transDescription: String, amount: Double, nav: Double, units: Double,
                              transType: BuySellEnum)

case class IRRData(folioNo: String, fundName: String, plan: Option[String], divFreq: Option[String], divOption: Option[String], xirr: Double)


object TransactionJsonFormats {
  implicit val transactionDetailsFormat = Json.format[TransactionDetails]
  implicit val transactionFormat = Json.format[Transaction]
  implicit val irrDataFormat = Json.format[IRRData]
}