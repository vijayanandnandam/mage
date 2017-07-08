package controllers

import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Named

import com.google.inject.Inject
import constants.{DBConstants, MongoConstants, OrderConstants}
import data.model.Tables.FcsiaRow
import helpers.{AuthenticatedAction, MailHelper, PoolingClientConnectionManager, SchemeHelper}
import models.enumerations.{InvestmentModeEnum, RedemptionModeEnum}
import models._
import models.RedemptionJsonFormats._
import models.CheckoutJsonFormats._
import models.OrderJsonFormats._
import org.apache.http.client.methods.HttpGet
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import play.api.mvc.{Action, Controller}
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json.BSONFormats
import repository.module.{IntegrationRepository, UserRepository}
import repository.tables.FcubdRepo
import service._
//import play.modules.reactivemongo.json.ImplicitBSONHandlers._
import reactivemongo.play.json.BSONFormats._
import service.integration.RelianceIntegrationServiceImpl
import utils.{Calculation, DBConstantsUtil, RequestUtils}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source
import scala.math.BigDecimal.RoundingMode
import scala.math.BigDecimal.RoundingMode.RoundingMode
import scala.util.{Failure, Success}

/**
  * Created by Fincash on 27-04-2017.
  */
class RedemptionController @Inject()(implicit ec: ExecutionContext, @Named("externalPoolingClientConnectionManager") relPoolingClient: PoolingClientConnectionManager, auth: AuthenticatedAction, mongoDbService: MongoDbService, checkoutService: CheckoutService, userRepository: UserRepository, fcubdRepo: FcubdRepo, userService: UserService,
                                     orderService: OrderService, schemeHelper: SchemeHelper, mailService: MailService, mailHelper: MailHelper, configuration: play.api.Configuration, relianceIntegrationServiceImpl: RelianceIntegrationServiceImpl,
                                     interationRepository: IntegrationRepository)
  extends Controller with OrderConstants with DBConstants with MongoConstants {

  val logger, log = LoggerFactory.getLogger(classOf[RedemptionController])
  val URL = PropertiesLoaderService.getConfig().getString("reliance.redemption.url")

  def collection(name: String) = mongoDbService.collection(name)

  /*def getRedemptionData = auth.Action.async(parse.json) { request =>

    val cartId = (request.body \"cartId").as[String]

    userService.getUserObjectFromReq(request).flatMap(userObject => {
      for {
        fundsList <- checkoutService.getCheckoutFunds(collection(CHECKOUT_COLLECTION_NAME) , userObject.get.username.get, cartId)
        nominee <- checkoutService.getCheckoutNominee(userObject.get.userid.get)
        bank <- checkoutService.getCheckoutBanks(userObject.get.userid.get)
      } yield {

        if (nominee.isDefined) {
          val checkout: Checkout = new Checkout(
            cartId,
            fundsData = fundsList.toList,
            nomineeList = Some(List.apply(nominee.get)),
            bankList = List.apply(bank));
          Ok(Json.toJson(checkout));
        } else {
          val checkout: Checkout = new Checkout(cartId,
            fundsData = fundsList.toList,
            nomineeList = None,
            bankList = List.apply(bank));
          Ok(Json.toJson(checkout));
        }

      }
    })
  }*/

  def getRedemptionDataSummary = auth.Action.async(parse.json) { request =>
    val redemptionId = (request.body \ "redemptionId").as[String]
    var redObjId = BSONObjectID.generate()
    if (redemptionId.length > 0) {
      redObjId = BSONObjectID.parse(redemptionId).get
    }

    userService.getUserObjectFromReq(request).flatMap(userObject => {
      if (userObject.nonEmpty) {
        var username = userObject.get.username.getOrElse("")
        var findQuery = BSONDocument("_id" -> redObjId, "username" -> username)
        //        var findQuery = BSONDocument("username" -> username)
        collection(REDEMPTION_COLLECTION_NAME).flatMap { coll => {
          coll.find(findQuery).one[BSONDocument].flatMap(doc => {
            if (doc.isEmpty) {
              for {
                nominee <- checkoutService.getCheckoutNominee(userObject.get.userid.get)
                bank <- checkoutService.getCheckoutBanks(userObject.get.userid.get)
              } yield {
                var nomineeData: Option[List[CheckoutNominee]] = None
                if (nominee.isDefined) {
                  nomineeData = Some(List.apply(nominee.get))
                }
                val redemption: Redemption = new Redemption(BSONObjectID.generate().stringify, fundsData = List[RedemptionFund](), nomineeList = nomineeData, bankList = List.apply(bank))
                Ok(Json.toJson(redemption))
              }
            }
            else {
              if (doc.get.contains("redemption")) {
                val redemption = (BSONFormats.BSONDocumentFormat.writes(doc.get).as[JsObject] \ "redemption").as[JsObject]
                logger.debug("redemption >> " + redemption)
                Future {
                  Ok(Json.toJson(redemption))
                }
              }
              else {
                for {
                  nominee <- checkoutService.getCheckoutNominee(userObject.get.userid.get)
                  bank <- checkoutService.getCheckoutBanks(userObject.get.userid.get)
                } yield {
                  var nomineeData: Option[List[CheckoutNominee]] = None
                  if (nominee.isDefined) {
                    nomineeData = Some(List.apply(nominee.get))
                  }
                  val redemption: Redemption = new Redemption(redemptionId, fundsData = List[RedemptionFund](), nomineeList = nomineeData, bankList = List.apply(bank))
                  Ok(Json.toJson(redemption))
                }
              }
            }
          })
        }
        }
      }
      else {
        logger.error("Error :: User doesn't exist")
        Future {
          Ok(Json.obj("success" -> false))
        }
      }
    })

    /*collection(REDEMPTION_COLLECTION_NAME).flatMap { coll =>
      coll.find(findQuery).one[BSONDocument].flatMap(doc => {
        if (doc.isEmpty) {
          userService.getUserObjectFromReq(request).flatMap(userObject => {
            for {
            //              fundsList <- checkoutService.getCheckoutFunds(collection(CHECKOUT_COLLECTION_NAME), userObject.get.username.get, cartId)
              nominee <- checkoutService.getCheckoutNominee(userObject.get.userid.get)
              bank <- checkoutService.getCheckoutBanks(userObject.get.userid.get)
            } yield {

              if (nominee.isDefined) {
                val redemption: Redemption = new Redemption(
                  "",
                  fundsData = List[RedemptionFund](), //fundsList.toList,
                  nomineeList = Some(List.apply(nominee.get)),
                  bankList = List.apply(bank))
                Ok(Json.toJson(redemption))
              } else {
                val redemption: Redemption = new Redemption(
                  "",
                  fundsData = List[RedemptionFund](), //fundsList.toList,
                  nomineeList = None,
                  bankList = List.apply(bank))
                Ok(Json.toJson(redemption))
              }
            }
          })
          //          Ok(Json.toJson(new Redemption("", List[RedemptionFund](), None, List[CheckoutBank](), None, None, None)))
        } else {
          if (doc.get.contains("redemption")) {
            val redemption = (BSONFormats.BSONDocumentFormat.writes(doc.get).as[JsObject] \ "redemption").as[JsObject]
            logger.debug("redemption >> " + redemption)
            Future {
              Ok(Json.toJson(redemption))
            }
          }
          else {
            userService.getUserObjectFromReq(request).flatMap(userObject => {
              for {
              //              fundsList <- checkoutService.getCheckoutFunds(collection(CHECKOUT_COLLECTION_NAME), userObject.get.username.get, cartId)
                nominee <- checkoutService.getCheckoutNominee(userObject.get.userid.get)
                bank <- checkoutService.getCheckoutBanks(userObject.get.userid.get)
              } yield {
                if (nominee.isDefined) {
                  val redemption: Redemption = new Redemption(
                    redemptionId,
                    fundsData = List[RedemptionFund](), //fundsList.toList,
                    nomineeList = Some(List.apply(nominee.get)),
                    bankList = List.apply(bank))
                  Ok(Json.toJson(redemption))
                } else {
                  val redemption: Redemption = new Redemption(
                    redemptionId,
                    fundsData = List[RedemptionFund](), //fundsList.toList,
                    nomineeList = None,
                    bankList = List.apply(bank))
                  Ok(Json.toJson(redemption))
                }
              }
            })
            //            Future{Ok(Json.toJson(new Redemption("", List[RedemptionFund](), None, List[CheckoutBank](), None, None, None)))}
          }
        }
      })
    }*/
  }

  def saveRedemptionData = auth.Action.async(parse.json) { request => {
    val id = (request.body \ "redemptionId").as[String]
    val redemption = (request.body \ "redemption").as[JsObject]
    var redemptionId: Option[BSONObjectID] = None
    if (id.length > 0) {
      redemptionId = Some(BSONObjectID.parse(id).get)
    } else {
      redemptionId = Some(BSONObjectID.generate())
    }

    userService.getUsernameFromRequest(request).flatMap { usernameOpt => {
      val username = usernameOpt.getOrElse("Anonymous")
      /*var findQuery = BSONDocument("username" -> username, "_id" -> redemptionId);*/
      val findQuery = BSONDocument("_id" -> redemptionId.get)
      val modifier = BSONDocument(
        "_id" -> redemptionId.get,
        "username" -> username,
        "redemption" -> redemption
      )

      collection(REDEMPTION_COLLECTION_NAME).flatMap { coll =>
        coll.find(findQuery).one[BSONDocument].flatMap(doc => {
          if (doc.isEmpty) {
            logger.debug("Redemption cart not found, creating new one with id  >>> " + redemptionId.get.stringify)
            val writeRes: Future[WriteResult] = mongoDbService.insertDoc(coll, modifier)
            writeRes.onComplete {
              case Failure(e) => {
                logger.error("Mongo Error :: " + e.getMessage + " in saving >>> " + redemptionId.get.stringify)
                Ok(Json.obj("success" -> false, "error" -> e.getMessage, "message" -> e.getMessage))
              }
              case Success(writeResult) => {
                logger.debug("successfully inserted document with result " + redemptionId.get.stringify)
              }
            }
            writeRes.map(_ => {
              Ok(Json.obj("redemptionId" -> redemptionId.get.stringify, "success" -> true))
            })
          }
          else {
            logger.debug("redemption id " + redemptionId.get.stringify)
            val writeRes: Future[UpdateWriteResult] = mongoDbService.updateDoc(coll, findQuery, BSONDocument("username" -> username, "redemption" -> redemption))
            writeRes.onComplete {
              case Failure(e) => {
                logger.error("Mongo Error :: " + e.getMessage + " in saving >>> " + redemptionId.get.stringify)
                Ok(Json.obj("success" -> false, "error" -> e.getMessage, "message" -> e.getMessage))
              }
              case Success(writeResult) => {
                logger.debug("successfully inserted document with result " + redemptionId.get.stringify)
              }
            }
            writeRes.map(_ => {
              Ok(Json.obj("redemptionId" -> redemptionId.get.stringify, "success" -> true))
            })
          }
        })
      }
    }
    }
  }
  }

  def redeem = auth.Action.async(parse.json) { request =>
    userService.getUserObjectFromReq(request).flatMap(userObject => {
      val summaryData = request.body.as[RedemptionSummary]
      logger.debug("summaryData >>>>>  ", summaryData)
      val subOrderList: ListBuffer[SubOrder] = ListBuffer[SubOrder]()
      summaryData.fundList.zipWithIndex.foreach(data => {

        val fund = data._1
        val index = data._2
        var amount: Option[Double] = None
        var quantity: Option[Double] = None
        if (fund.amountUnitFlag) {
          amount = Some(fund.amount)
        }
        else {
          quantity = Some(fund.amount)
        }
        var allRedeem: Option[Boolean] = Some(fund.fullPartialFlag)
        val redMode = LUMPSUM_REDEMPTION_MODE

        // SET INSTA Mode
        var instaMode = false
        if (fund.selectedRedemptionMode.nonEmpty && fund.selectedRedemptionMode.get == INSTA_REDEMPTION_MODE) {
          // Partial flag
          /*if (!fund.fullPartialFlag) {*/
          // IF AMOUNT
          if (amount.nonEmpty) {
            // IF holding > 50K
            if (fund.currValue.nonEmpty && fund.currValue.get > 50000) {
              // If 90% of holding > 50K
              if (Calculation.percentageRounded(89.98, fund.currValue.get) > 50000) {
                // If amount entered is less than 50K
                if (amount.get <= 50000) {
                  subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = quantity,
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(true))))
                }
                // If amount entered is > 50K
                else {
                  subOrderList.+=(SubOrder(index, Some(50000), redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = quantity,
                    folioNo = Some(fund.folioNo),
                    allRedeem = Some(false), // allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(true))))
                  subOrderList.+=(SubOrder(index, Some(Calculation.createRounded(amount.get).doubleValue() - 50000), redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = quantity,
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(false))))
                }
              }
              // If 90% of holding < 50K
              else {
                // If amount entered is less than 90% of holding
                if (Calculation.createRounded(amount.get) <= Calculation.percentageRounded(89.98, fund.currValue.get)) {
                  subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = quantity,
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(true))))
                }
                // If amount entered is greater than 90% of holding
                else {
                  subOrderList.+=(SubOrder(index, Some(Calculation.percentageRounded(89.98, fund.currValue.get).doubleValue()), redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = quantity,
                    folioNo = Some(fund.folioNo),
                    allRedeem = Some(false), // allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(true))))
                  subOrderList.+=(SubOrder(index, Some((Calculation.createRounded(amount.get) - Calculation.percentageRounded(89.98, fund.currValue.get)).doubleValue()), redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = quantity,
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(false))))
                }
              }
            }
            // If holding <= 50K
            if (fund.currValue.nonEmpty && fund.currValue.get <= 50000) {
              // If amount entered is less than 90% of holding
              if (Calculation.createRounded(amount.get) <= Calculation.percentageRounded(89.98, fund.currValue.get)) {
                subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                  quantity = quantity,
                  folioNo = Some(fund.folioNo),
                  allRedeem = allRedeem,
                  additionalDetails = Some(SubOrderAdditionalDetails(true))))
              }
              // If amount entered is greater than 90% of holding
              else {
                subOrderList.+=(SubOrder(index, Some(Calculation.percentageRounded(89.98, fund.currValue.get).doubleValue()), redMode, fund.option.soptRfnum, None, N_FLAG,
                  quantity = quantity,
                  folioNo = Some(fund.folioNo),
                  allRedeem = Some(false), // allRedeem,
                  additionalDetails = Some(SubOrderAdditionalDetails(true))))
                subOrderList.+=(SubOrder(index, Some((Calculation.createRounded(amount.get) - Calculation.percentageRounded(89.98, fund.currValue.get)).doubleValue()), redMode, fund.option.soptRfnum, None, N_FLAG,
                  quantity = quantity,
                  folioNo = Some(fund.folioNo),
                  allRedeem = allRedeem,
                  additionalDetails = Some(SubOrderAdditionalDetails(false))))
              }
            }
          }
          // IF UNITS
          if (quantity.nonEmpty) {
            // If holding > 50K
            if (fund.redeemableUnits.nonEmpty && fund.currNav.nonEmpty && fund.redeemableUnits.get * fund.currNav.get > 50000) {
              // If 90% of holding > 50K
              if (Calculation.percentageRounded(89.98, fund.redeemableUnits.get * fund.currNav.get) > 50000) {
                // If value of units entered is less than 50K
                if (Calculation.createRounded(quantity.get * fund.currNav.get) <= 50000) {
                  subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = quantity,
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(true))))
                }
                // If value of units entered is > 50K
                else {
                  val unitsForFiftyK = Calculation.divideRounded(50000, fund.currNav.get)
                  subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = Some(unitsForFiftyK.doubleValue()),
                    folioNo = Some(fund.folioNo),
                    allRedeem = Some(false), // allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(true))))
                  subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = Some((quantity.get - unitsForFiftyK).doubleValue()),
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(false))))
                }
              }
              // If 90% of holding < 50K
              else {
                // If entered units(fund.amount can have amount/units) is less than 90% of quantity(redeemable)
                if (quantity.get <= Calculation.rounded(Calculation.percentageRounded(89.98, fund.redeemableUnits.get), RoundingMode.HALF_EVEN, 3) ) {
                  subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = quantity,
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(true))))
                }
                // If entered units is less than 90% of quantity(redeemable)
                else {
                  subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = Some(Calculation.rounded(Calculation.percentageRounded(89.98, fund.redeemableUnits.get), RoundingMode.HALF_EVEN, 3).doubleValue()),
                    folioNo = Some(fund.folioNo),
                    allRedeem = Some(false), // allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(true))))
                  subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = Some((quantity.get - Calculation.rounded(Calculation.percentageRounded(89.98, fund.redeemableUnits.get), RoundingMode.HALF_EVEN, 3)).doubleValue()),
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(false))))
                }
              }
            }
            // if holding <= 50K
            if (fund.redeemableUnits.nonEmpty && fund.currNav.nonEmpty && fund.redeemableUnits.get * fund.currNav.get <= 50000) {
              // If entered units(fund.amount can have amount/units) is less than 90% of quantity(redeemable)
              if (quantity.get <= Calculation.rounded(Calculation.percentageRounded(89.98, fund.redeemableUnits.get), RoundingMode.HALF_EVEN, 3)) {
                subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                  quantity = quantity,
                  folioNo = Some(fund.folioNo),
                  allRedeem = allRedeem,
                  additionalDetails = Some(SubOrderAdditionalDetails(true))))
              }
              // If entered units is less than 90% of quantity(redeemable)
              else {
                subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                  quantity = Some(Calculation.rounded(Calculation.percentageRounded(89.98, fund.redeemableUnits.get), RoundingMode.HALF_EVEN, 3).doubleValue()),
                  folioNo = Some(fund.folioNo),
                  allRedeem = Some(false), // allRedeem,
                  additionalDetails = Some(SubOrderAdditionalDetails(true))))
                subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                  quantity = Some((quantity.get - Calculation.rounded(Calculation.percentageRounded(89.98, fund.redeemableUnits.get), RoundingMode.HALF_EVEN, 3)).doubleValue()),
                  folioNo = Some(fund.folioNo),
                  allRedeem = allRedeem,
                  additionalDetails = Some(SubOrderAdditionalDetails(false))))
              }
            }
          }
          /* }
           // ALL Redeem
           else {

           }*/
        }

        else {
          // MAKE SUBORDERLIST directly
          subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
            quantity = quantity,
            folioNo = Some(fund.folioNo),
            allRedeem = allRedeem,
            additionalDetails = Some(SubOrderAdditionalDetails(instaMode))))
        }

        /*} else {
          val redMode = SWP_REDEMPTION_MODE
          val frequency = schemeHelper.getSipFrequencyShortForm(fund.selectedAWPFrequency.get)
          subOrderList.+=(SubOrder(index, None, redMode, fund.option.soptRfnum, None, N_FLAG,
            sipDayOfMonth = Some(fund.selectedAWPDate.get.toInt),
            sipFrequency = Some(frequency),
            sipNoOfInstallments = Some(fund.noOfInstallment.get.toInt),
            orderType = Some(BUYSELL_SWP),
            quantity = quantity,
            transactionMode = Some(RELIANCE_TRANSACTION_MODE),
            folioNo = Some(fund.folioNo)))
        }*/
      })

      val orderModel = OrderModel(BUYSELL_SELL, None, Some(RequestUtils.getIpAddress(request)), subOrderList.toList, None, Some(summaryData.bankRfnum),
        Some(summaryData.imagePath))

      orderService.placeNewFinOrder(orderModel, userObject.get).flatMap(orderDetailsTuple => {
        val orderId = orderDetailsTuple._2.id
        userService.getUserMobileNo(userObject.get.userid.get).map(mobileNo => {
          Ok(Json.obj("success" -> true, "mob" -> mobileNo, "orderId" -> orderId))
        })
      })

    });

  }

  def redeemInExchange = auth.Action.async(parse.json) { request =>
    userService.getUserObjectFromReq(request).flatMap(userObject => {
      val exchangeRedemptionSummary = request.body.as[ExchangeRedemptionSummary]
      val summaryData = exchangeRedemptionSummary.redemptionSummary
      val orderId = exchangeRedemptionSummary.orderId
      val subOrderList: ListBuffer[SubOrder] = ListBuffer[SubOrder]()
      summaryData.fundList.zipWithIndex.foreach(data => {

        val fund = data._1
        val index = data._2

        // SET AMOUNT/QUANTITY
        var amount: Option[Double] = None
        var quantity: Option[Double] = None
        if (fund.amountUnitFlag) {
          amount = Some(fund.amount)
        }
        else {
          quantity = Some(fund.amount)
        }

        var allRedeem: Option[Boolean] = Some(fund.fullPartialFlag)
        val redMode = LUMPSUM_REDEMPTION_MODE

        // SET INSTA Mode
        var instaMode = false
        if (fund.selectedRedemptionMode.nonEmpty && fund.selectedRedemptionMode.get == INSTA_REDEMPTION_MODE) {
          // Partial flag
          /*if (!fund.fullPartialFlag) {*/
          // IF AMOUNT
          if (amount.nonEmpty) {
            // IF holding > 50K
            if (fund.currValue.nonEmpty && fund.currValue.get > 50000) {
              // If 90% of holding > 50K
              if (Calculation.percentageRounded(89.98, fund.currValue.get) > 50000) {
                // If amount entered is less than 50K
                if (amount.get <= 50000) {
                  subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = quantity,
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(true))))
                }
                // If amount entered is > 50K
                else {
                  subOrderList.+=(SubOrder(index, Some(50000), redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = quantity,
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(true))))
                  subOrderList.+=(SubOrder(index, Some(Calculation.createRounded(amount.get).doubleValue() - 50000), redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = quantity,
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(false))))
                }
              }
              // If 90% of holding < 50K
              else {
                // If amount entered is less than 90% of holding
                if (Calculation.createRounded(amount.get) <= Calculation.percentageRounded(89.98, fund.currValue.get)) {
                  subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = quantity,
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(true))))
                }
                // If amount entered is greater than 90% of holding
                else {
                  subOrderList.+=(SubOrder(index, Some(Calculation.percentageRounded(89.98, fund.currValue.get).doubleValue()), redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = quantity,
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(true))))
                  subOrderList.+=(SubOrder(index, Some((Calculation.createRounded(amount.get) - Calculation.percentageRounded(89.98, fund.currValue.get)).doubleValue()), redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = quantity,
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(false))))
                }
              }
            }
            // If holding <= 50K
            if (fund.currValue.nonEmpty && fund.currValue.get <= 50000) {
              // If amount entered is less than 90% of holding
              if (Calculation.createRounded(amount.get) <= Calculation.percentageRounded(89.98, fund.currValue.get)) {
                subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                  quantity = quantity,
                  folioNo = Some(fund.folioNo),
                  allRedeem = allRedeem,
                  additionalDetails = Some(SubOrderAdditionalDetails(true))))
              }
              // If amount entered is greater than 90% of holding
              else {
                subOrderList.+=(SubOrder(index, Some(Calculation.percentageRounded(89.98, fund.currValue.get).doubleValue()), redMode, fund.option.soptRfnum, None, N_FLAG,
                  quantity = quantity,
                  folioNo = Some(fund.folioNo),
                  allRedeem = allRedeem,
                  additionalDetails = Some(SubOrderAdditionalDetails(true))))
                subOrderList.+=(SubOrder(index, Some((Calculation.createRounded(amount.get) - Calculation.percentageRounded(89.98, fund.currValue.get)).doubleValue()), redMode, fund.option.soptRfnum, None, N_FLAG,
                  quantity = quantity,
                  folioNo = Some(fund.folioNo),
                  allRedeem = allRedeem,
                  additionalDetails = Some(SubOrderAdditionalDetails(false))))
              }
            }
          }
          // IF UNITS
          if (quantity.nonEmpty) {
            // If holding > 50K
            if (fund.redeemableUnits.nonEmpty && fund.currNav.nonEmpty && fund.redeemableUnits.get * fund.currNav.get > 50000) {
              // If 90% of holding > 50K
              if (Calculation.percentageRounded(89.98, fund.redeemableUnits.get * fund.currNav.get) > 50000) {
                // If value of units entered is less than 50K
                if (Calculation.createRounded(quantity.get * fund.currNav.get) <= 50000) {
                  subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = quantity,
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(true))))
                }
                // If value of units entered is > 50K
                else {
                  val unitsForFiftyK = Calculation.divideRounded(50000, fund.currNav.get)
                  subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = Some(unitsForFiftyK.doubleValue()),
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(true))))
                  subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = Some((quantity.get - unitsForFiftyK).doubleValue()),
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(false))))
                }
              }
              // If 90% of holding < 50K
              else {
                // If entered units(fund.amount can have amount/units) is less than 90% of quantity(redeemable)
                if (quantity.get <= Calculation.percentageRounded(89.98, fund.redeemableUnits.get)) {
                  subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = quantity,
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(true))))
                }
                // If entered units is less than 90% of quantity(redeemable)
                else {
                  subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = Some(Calculation.percentageRounded(89.98, fund.redeemableUnits.get).doubleValue()),
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(true))))
                  subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                    quantity = Some((quantity.get - Calculation.percentageRounded(89.98, fund.redeemableUnits.get)).doubleValue()),
                    folioNo = Some(fund.folioNo),
                    allRedeem = allRedeem,
                    additionalDetails = Some(SubOrderAdditionalDetails(false))))
                }
              }
            }
            // if holding <= 50K
            if (fund.redeemableUnits.nonEmpty && fund.currNav.nonEmpty && fund.redeemableUnits.get * fund.currNav.get <= 50000) {
              // If entered units(fund.amount can have amount/units) is less than 90% of quantity(redeemable)
              if (quantity.get <= Calculation.percentageRounded(89.98, fund.redeemableUnits.get)) {
                subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                  quantity = quantity,
                  folioNo = Some(fund.folioNo),
                  allRedeem = allRedeem,
                  additionalDetails = Some(SubOrderAdditionalDetails(true))))
              }
              // If entered units is less than 90% of quantity(redeemable)
              else {
                subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                  quantity = Some(Calculation.percentageRounded(89.98, fund.redeemableUnits.get).doubleValue()),
                  folioNo = Some(fund.folioNo),
                  allRedeem = allRedeem,
                  additionalDetails = Some(SubOrderAdditionalDetails(true))))
                subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
                  quantity = Some((quantity.get - Calculation.percentageRounded(89.98, fund.redeemableUnits.get)).doubleValue()),
                  folioNo = Some(fund.folioNo),
                  allRedeem = allRedeem,
                  additionalDetails = Some(SubOrderAdditionalDetails(false))))
              }
            }
          }
          /* }
           // ALL Redeem
           else {

           }*/
        }

        else {
          // MAKE SUBORDERLIST directly
          subOrderList.+=(SubOrder(index, amount, redMode, fund.option.soptRfnum, None, N_FLAG,
            quantity = quantity,
            folioNo = Some(fund.folioNo),
            allRedeem = allRedeem,
            additionalDetails = Some(SubOrderAdditionalDetails(instaMode))))
        }
      })

      val orderModel = OrderModel(BUYSELL_SELL, None, Some(RequestUtils.getIpAddress(request)), subOrderList.toList, None, Some(summaryData.bankRfnum),
        Some(summaryData.imagePath))

      orderService.prepareAndPlaceNewExchangeOrder(orderModel, orderId, userObject.get).map(processedOrderModel => {
        val modifiedSubOrderList: ListBuffer[ProcessedSubOrderModel] = ListBuffer[ProcessedSubOrderModel]()
        processedOrderModel.subOrderList.foreach(subOrder => {
          modifiedSubOrderList.+=(subOrder.copy(investmentMode = DBConstantsUtil.getInvestmentModeFullForm(subOrder.investmentMode)))
        })

        logger.debug(Json.toJson(processedOrderModel.copy(subOrderList = modifiedSubOrderList.toList)).toString())
        Ok(Json.toJson(processedOrderModel.copy(subOrderList = modifiedSubOrderList.toList)))
      })

    });

  }

  def removeFundFromCart = auth.Action.async(parse.json) { request => {
    var requestData = request.body
    logger.debug(request.body.toString())
    val soptRfnum = requestData.\("id").get.as[String]
    val redemptionId = requestData.\("redemptionId").get.as[String]
    val folioNo = (requestData.\("folioNo")).get.as[String]

    var username: Option[String] = None
    userService.getUserObjectFromReq(request).flatMap(user => {
      if (user.nonEmpty) {
        username = user.get.username
        collection(REDEMPTION_COLLECTION_NAME).flatMap(coll => {
          if (redemptionId.length == 0 && soptRfnum.length == 0) {
            logger.debug("redemptionId OR soptRfnum not provided cartid [" + redemptionId + "] sopt [" + soptRfnum + "]");
            Future.apply(None)
          }
          val findQuery = BSONDocument("_id" -> BSONObjectID.parse(redemptionId).get)
          coll.find(findQuery).one[BSONDocument].map(doc => {
            if (doc.nonEmpty) {
              if (doc.get.contains("redemption")) {
                var redemption = (BSONFormats.BSONDocumentFormat.writes(doc.get).as[JsObject] \ "redemption").as[JsObject]
                if (redemption.keys.contains("fundsData")) {
                  var nomineeList = (redemption \ "nomineeList").as[List[CheckoutNominee]]
                  var bankList = (redemption \ "bankList").as[List[CheckoutBank]]
                  var selectedBank = (redemption \ "selectedBank").as[CheckoutBank]
                  var selectedNominee = (redemption \ "selectedNominee").as[CheckoutNominee]
                  var fundsData = (redemption \ "fundsData").as[List[RedemptionFund]]
                  fundsData = fundsData.filterNot(x => x.option.soptRfnum.toString == soptRfnum && x.folioNo == folioNo)
                  redemption = (new Redemption(redemptionId, fundsData, Some(nomineeList), bankList, Some(selectedBank), Some(selectedNominee))).asInstanceOf[JsObject]
                  val writeRes: Future[UpdateWriteResult] = mongoDbService.updateDoc(coll, findQuery, BSONDocument("username" -> username, "redemption" -> redemption))
                  writeRes.onComplete {
                    case Failure(e) => {
                      logger.error("Mongo Error :: " + e.getMessage + " in saving >>> " + redemptionId)
                      Ok(Json.obj("success" -> false, "error" -> e.getMessage, "message" -> e.getMessage))
                    }
                    case Success(writeResult) => {
                      logger.debug("successfully inserted document with result " + redemptionId)
                    }
                  }
                  writeRes.map(_ => {
                    Ok(Json.obj("redemptionId" -> redemptionId, "success" -> true))
                  })
                }
                Ok(Json.obj("success" -> false, "message" -> "'redemption' does not have key 'fundsData'"))
              }
              Ok(Json.obj("success" -> false, "message" -> "Doc does not have key 'redemption'"))
            }
            // Document is empty
            else {
              Ok(Json.obj("success" -> false, "message" -> "Redemption document empty"))
            }
          })
        })
      }
      else {
        Future {
          Ok(Json.obj("message" -> "User not found", "error" -> "User not found", "success" -> false))
        }
      }
    })
  }
  }

  def getRelianceInstaStatus = auth.Action(parse.json) { request => {
    var requestData = request.body
    val refNo = requestData.\("refno").get.as[String]
    val redemptionUrl: String = URL
    val parameterUrl = "fund=RMF&refno=" + refNo + "&deviceid=PARTNERAPI&appVersion=1.0.1&appName=FINCASH&apikey=2665ae4f-0583-4872-a8d1-07815cb938ce"
    val finalUrl = redemptionUrl + "Getrefnostatus?" + parameterUrl

    val httpGet = new HttpGet(finalUrl)
    //httpGet.setURI(finalUrl)
    val httpClient = relPoolingClient.getHttpClient
    try {
      val httpResponse = httpClient.execute(httpGet)
      if (httpResponse.getStatusLine().getStatusCode() == 200) {
        val inputStream = httpResponse.getEntity.getContent
        val res: JsValue = Json.parse(inputStream)
        println("OUTPUT >> ", res)
        Ok(Json.obj("Output" -> res))
      }
      else {
        Ok(Json.obj("output" -> "Status code not 200"))
      }
    }
    catch {
      case io: IOException => {
        logger.debug("IO exception >> ", io);
        Ok(Json.obj("output" -> "exception occurred"))
      }
    }
  }
  }

  def checkInstaProducts = auth.Action(parse.json) { request => {
    val redFunds = (request.body \ "redFunds").as[List[RedemptionFund]]
    var counter = 0
    val instaList = ListBuffer[Long]()
    var a = Future.sequence(for (redFund <- redFunds) yield {
      interationRepository.getSchemeOrderIntegration(redFund.option.soptRfnum, BUYSELL_SELL).map(integrationList => {
        if (integrationList.nonEmpty) {
          counter = counter + 1
          integrationList.foreach(value => {
            val smtrfnum = value.siasmtrfnum
            instaList.+=(smtrfnum)
          })
        }
      })
    })

    Await.result(a, Duration.Inf)
    if (counter > 0) {
      Ok(Json.obj("instaExists" -> true, "instaList" -> instaList))
    }
    else {
      Ok(Json.obj("instaExists" -> false, "instaList" -> List.empty[Long]))
    }

  }}

}