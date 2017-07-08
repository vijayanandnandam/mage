package models

import models.enumerations.InvestmentModeEnum.InvestmentModeEnum
import models.enumerations.ProductEnum.ProductEnum
import play.api.libs.json.{JsObject, Json}
import models.FundsJsonFormats._
import reactivemongo.play.json._
import reactivemongo.bson.{BSONArray, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONDouble, BSONInteger, BSONLong, BSONObjectID, BSONString}
import play.modules.reactivemongo.json.ImplicitBSONHandlers._

//case class CartFund(fundId: Long, fundName: String, investmentModeEnum: String, risk: String, minInvestment: Double)
case class Cart(id: String, funds: Option[List[JsObject]])

case class CartItem(product: ProductEnum, investmentType: InvestmentModeEnum, fundIds: Seq[Int])

case class CartItemDB(_id: BSONObjectID, product: ProductEnum, investmentType: InvestmentModeEnum, fundIds: Seq[Int])

case class CartProduct(product: Int, investmentType: Int, investmentAmount: Option[BigDecimal],
                       investmentPeriod: Option[Int], funds: List[FundDoc])

object CartJsonFormats {
  implicit val cartFormat = Json.format[Cart]
  implicit val cartItemFormat = Json.format[CartItem]
  //  implicit val cartItemDBFormat = Json.format[CartItemDB]
  implicit val cartProductFormat = Json.format[CartProduct]
  //  implicit val cartFundFormat = Json.format[CartFund]
}

/*object CartFund {
  implicit object CartFundReader extends BSONDocumentReader[CartFund] {
    def read(doc: BSONDocument): CartFund = {
      val fundId = doc.getAs[Long]("fundId").get
      val fundName = doc.getAs[String]("fundName").get
      val investmentModeEnum = doc.getAs[String]("investmentModeEnum").get
      val risk = doc.getAs[String]("risk").get
      val minInvestment = doc.getAs[Double]("minInvestment").get
      CartFund(fundId, fundName, investmentModeEnum, risk, minInvestment)
    }
  }

  implicit object CartFundWriter extends BSONDocumentWriter[CartFund] {
    def write(cartFundObj: CartFund): BSONDocument = {
      val doc = BSONDocument(
        "fundId" -> BSONLong(cartFundObj.fundId),
        "fundName" -> BSONString(cartFundObj.fundName),
        "investmentModeEnum" -> BSONString(cartFundObj.investmentModeEnum),
        "risk" -> BSONString(cartFundObj.risk),
        "minInvestment" -> BSONDouble(cartFundObj.minInvestment)
      )
      doc
    }
  }
}*/
object Cart {
 /* //  import CartJsonFormats.cartFundFormat
  import play.modules.reactivemongo.json.ImplicitBSONHandlers._
  import play.modules.reactivemongo.json.BSONFormats._

  implicit object CartReader extends BSONDocumentReader[Cart] {
    def read(doc: BSONDocument): Cart = {
      val id = doc.getAs[BSONObjectID]("_id").get
      val username = doc.getAs[BSONString]("username").get.value
      val funds = doc.getAs[BSONArray]("funds").get
      Cart(id, username, Some(funds))
    }
  }

  implicit object CartWriter extends BSONDocumentWriter[Cart] {
    def write(cartObj: Cart): BSONDocument = {
      val doc = BSONDocument(
        "username" -> cartObj.username,
        "funds" -> cartObj.funds
      )
      doc
    }
  }*/

}