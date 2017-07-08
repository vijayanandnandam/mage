package models

import models.enumerations.InvestmentModeEnum.InvestmentModeEnum
import models.enumerations.ProductEnum.ProductEnum
import play.api.libs.json.Json
import models.FundsJsonFormats._
import reactivemongo.play.json._
import reactivemongo.bson.BSONObjectID
import scala.concurrent.Future

case class WishlistItem(product: ProductEnum, investmentType: InvestmentModeEnum, fundIds: Seq[Int])
case class WishlistItemDB(_id: BSONObjectID, product: ProductEnum, investmentType: InvestmentModeEnum, fundIds: Seq[Int])
case class WishlistProduct(product: Int, investmentType: Int, schemes: List[SchemeModel])

object WishlistJsonFormats {
  implicit val wishlistItemFormat = Json.format[WishlistItem]
  implicit val wishlistItemDBFormat = Json.format[WishlistItemDB]
  implicit val wishlistProductFormat = Json.format[WishlistProduct]
}