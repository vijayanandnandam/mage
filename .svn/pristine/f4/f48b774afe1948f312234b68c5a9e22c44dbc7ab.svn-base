package models

case class ProductStateObject(soptRfnumArray: String, soptIsDefaultArray: String, soptInvestmentModeArray: Option[String],
                              soptIsWeightage: Option[String], smtRfnum: Option[Long], productName: Option[String],
                              productRfnum: Option[Long])

case class ProductOption(soptRfnum: Long, isDefault: String, weightage: Option[Double], investmentMode: Option[String])

case class Product(productRfnum: Long, productName: String, smtRfnum: Long, productOptions: List[ProductOption])

object ProductsJsonFormats {

  import play.api.libs.json.Json

  implicit val productStateObjectFormat = Json.format[ProductStateObject];
  implicit val productOptionFormat = Json.format[ProductOption];
  implicit val productFormat = Json.format[Product];


}