package service.integration

import javax.inject.{Inject, Singleton}

import constants.{IntegrationConstants, OrderConstants}
import data.model.Tables.{FcomtRow, FcsotRow}
import helpers.OrderHelper
import models._
import org.slf4j.LoggerFactory
import repository.module.{FolioRepository, IntegrationRepository}
import service.SchemeService

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


/**
  * Created by fincash on 19-04-2017.
  */

@Singleton
class IntegrationServiceImpl @Inject()(implicit ec: ExecutionContext,
                                       bSEIntegrationServiceImpl: BSEIntegrationServiceImpl, relianceIntegrationServiceImpl: RelianceIntegrationServiceImpl,
                                       birlaIntegrationServiceImpl: BirlaIntegrationServiceImpl, integrationRepository: IntegrationRepository,
                                       schemeService: SchemeService, folioRepository: FolioRepository,orderHelper: OrderHelper)
  extends IntegrationConstants with OrderConstants {

  val logger, log = LoggerFactory.getLogger(classOf[IntegrationServiceImpl])


  val integrationMap: mutable.HashMap[Long, IntegrationService] = new mutable.HashMap[Long, IntegrationService]()
  integrationMap.+=(BSE_INTEGRATION_VALUE -> bSEIntegrationServiceImpl)
  integrationMap.+=(RELIANCE_INTEGRATION_VALUE -> relianceIntegrationServiceImpl)
  integrationMap.+=(BIRLA_INTEGRATION_VALUE -> birlaIntegrationServiceImpl)

  /**
    *
    * @param orderModel
    * @param subOrderModel
    * @param subOrder
    * @return
    */
  def getOrderIntegration(orderModel: OrderModel,subOrderModel:SubOrder,subOrder:FcsotRow):Future[Long] = {

    schemeService.getSchemeIdByOptionId(subOrder.sotsoptrfnum.toString).flatMap(smtrfnum => {

      integrationRepository.getSchemeOrderIntegration(subOrder.sotsoptrfnum, orderModel.orderType).map(integrationTypeList => {
        var integrationType = BSE_INTEGRATION_VALUE
        if (orderModel.orderType == BUYSELL_SELL) {
          if (integrationTypeList.nonEmpty && subOrderModel.additionalDetails.get.instantFlag) {
            integrationType = integrationTypeList.head.siaimtrfnum
          }
        } else {
          if (integrationTypeList.nonEmpty && subOrderModel.allRedeem.isEmpty) {
            integrationType = integrationTypeList.head.siaimtrfnum
          }
        }
        integrationType
      })
    })
  }

  /**
    *
    * @param orderModel
    * @param order
    * @param subOrderList
    * @param orderStateChangeMap
    * @param userLoginObject
    * @return
    */
  def placeSubOrders(orderModel: OrderModel, order: FcomtRow, subOrderList: List[FcsotRow], orderStateChangeMap: mutable.HashMap[Long, List[Long]], userLoginObject: UserLoginObject): Future[List[ProcessedSubOrderModel]] = {

    val zippedSubOrderList = subOrderList.zipWithIndex
    processMandate(orderModel,subOrderList,userLoginObject).flatMap(_ =>{

      val subOrderProcessedListFtr = for (subOrderTuple <- zippedSubOrderList) yield {
        val subOrder = subOrderTuple._1
        val subOrderIdx = subOrderTuple._2
        val subOrderModel = orderModel.subOrders(subOrderIdx)

        isValidForIntegration(orderModel,subOrderModel,subOrder,order, orderStateChangeMap, userLoginObject).flatMap(isValidOrder =>{

          logger.debug("Is Order Valid for Integration = " + isValidOrder)
          if(isValidOrder){

            getOrderIntegration(orderModel,subOrderModel,subOrder).flatMap(integrationType =>{
              val integrationTypeService = integrationMap.getOrElse(integrationType, bSEIntegrationServiceImpl)
              isIntegrationRunning(integrationType).flatMap(integrationRunning =>{
                logger.debug("Integration Running = " + integrationRunning)
                if(!integrationRunning){
                  logger.debug("Queuing order in system")
                  queueIntegrationOrder(subOrder, order, orderStateChangeMap, userLoginObject).map(value =>{
                    val buySellTypeName = orderHelper.getBuySellTypeName(order.omtbuysell, subOrder.sotbuyselltype)
                    ProcessedSubOrderModel(subOrder.id, ORDER_STATUS_SUCCESS, subOrder.sotsoptrfnum, subOrder.sotinvestmentmode, order.omtbuysell, buySellTypeName)
                  })
                } else{
                  integrationTypeService.placeSubOrders(orderModel, order, subOrder, subOrderModel, orderStateChangeMap, userLoginObject).flatMap(orderProcessModel => {

                    integrationRepository.getIntegrationFallbackAllowed(integrationType).flatMap(isFallbackAllowed => {
                      if (orderProcessModel.orderProcessed == ORDER_CONNECTIVITY_FAILURE && isFallbackAllowed) {
                        bSEIntegrationServiceImpl.placeSubOrders(orderModel, order, subOrder, subOrderModel, orderStateChangeMap, userLoginObject).map(bseProcessedOrderModel => {
                          bseProcessedOrderModel
                        })
                      } else {
                        Future {
                          orderProcessModel
                        }
                      }
                    })
                  })
                }
              })

            })
          } else{
            val buySellTypeName = orderHelper.getBuySellTypeName(order.omtbuysell, subOrder.sotbuyselltype)
            Future.apply(ProcessedSubOrderModel(subOrder.id, ORDER_STATUS_SUCCESS, subOrder.sotsoptrfnum, subOrder.sotinvestmentmode, order.omtbuysell, buySellTypeName))
          }
        })
      }

      Future.sequence(subOrderProcessedListFtr)
    })
  }

  def processMandate(orderModel: OrderModel,subOrderList: List[FcsotRow], userLoginObject: UserLoginObject):Future[List[Any]] = {

    subOrderList.zipWithIndex.foldLeft(Future(List.empty[Any])){
      (prevFtr,subOrderIndexTuple) => for {

        prevResult <- prevFtr
        currFtr <- {

          val subOrder = subOrderIndexTuple._1
          val subOrderIdx = subOrderIndexTuple._2
          val subOrderModel = orderModel.subOrders(subOrderIdx)
          if(subOrder.sotinvestmentmode == SIP_INVESTMENT_MODE && subOrderModel.existingMmtRfnum.isEmpty){
            bSEIntegrationServiceImpl.processMandate(orderModel.buaRfnum,subOrder,userLoginObject).map(_ =>{
              Future{}
            })
          } else{
            Future{}
          }
        }
      } yield {prevResult :+ currFtr}
    }
  }
  /**
    *
    * @param integrationType
    * @return
    */
  def isIntegrationRunning(integrationType:Long):Future[Boolean] = {
    integrationRepository.isIntegrationRunning(integrationType).map(downTimeSeq => {
      logger.debug("Integration Res = [" + downTimeSeq.size + "]")
      downTimeSeq.size == 0
    })
  }

  /**
    *
    * @param sotRow
    * @param omtRow
    * @param orderStateChangeMap
    * @param userLoginObject
    * @return
    */
  def queueIntegrationOrder(sotRow:FcsotRow, omtRow: FcomtRow , orderStateChangeMap: mutable.HashMap[Long, List[Long]], userLoginObject: UserLoginObject): Future[(Boolean,String)] = {
    orderHelper.updateSubOrderState(sotRow, omtRow,ORDER_TO_BE_PLACED_LATER,orderStateChangeMap,ORDER_STATUS_SUCCESS,userLoginObject)
  }

  /**
    *
    * @param orderModel
    * @param subOrder
    * @param sotRow
    * @param omtRow
    * @param orderStateChangeMap
    * @param userLoginObject
    * @return
    */
  def isValidForIntegration(orderModel: OrderModel,subOrder:SubOrder, sotRow:FcsotRow, omtRow: FcomtRow , orderStateChangeMap: mutable.HashMap[Long, List[Long]], userLoginObject: UserLoginObject):Future[Boolean] = {

    if(orderModel.orderType == BUYSELL_SELL){
      return Future.apply(true)
    }

    val additionalDetails = subOrder.additionalDetails
    if(additionalDetails.nonEmpty){
      val isLOne = additionalDetails.get.isLOne.getOrElse(false)
      logger.debug("L1 Order = [" + isLOne + "]" )
      if(isLOne){
        orderHelper.isLOneLZeroOrderTimeValid(additionalDetails.get.lOneLZeroDetails.get,subOrder.buySchemeOptionRfnum).flatMap(valid =>{
          if(!valid){
            orderHelper.updateSubOrderState(sotRow, omtRow,ORDER_TO_BE_PLACED_LATER,orderStateChangeMap,ORDER_STATUS_SUCCESS,userLoginObject).map(value =>{
              valid
            })
          } else{
            Future.apply(valid)
          }

        })
      }
      else{
        Future.apply(true)
      }
    } else{
      Future.apply(true)
    }
  }

  /**
    *
    * @param subOrderModel
    * @return
    */
  def checkSavingsPlusFullRedemption(subOrderModel: SubOrder): Boolean = {
    var retval = true
    if (!subOrderModel.allRedeem.getOrElse(false)) {
      // true bse, false reliance
      var amount = subOrderModel.amount
      var units = subOrderModel.quantity
      var folioNo = subOrderModel.folioNo.getOrElse("")
      var soptrfnum = subOrderModel.buySchemeOptionRfnum

      val a = folioRepository.getFhtRowByFolioNoAndSoptrfnum(folioNo, soptrfnum).flatMap(fhtrowOption => {
        if (fhtrowOption.nonEmpty) {
          val holdingUnits = fhtrowOption.get.fhtholdingunits
          folioRepository.getCurrNavByFundId(soptrfnum).map(navValues => {
            val currNav = navValues._1
            val navDate = navValues._2
            val currValue = currNav * holdingUnits

            // if amount is non empty
            if (amount.nonEmpty) {
              if (amount.get > 0.90 * currValue) {
                retval = true
              }
              else
                retval = false
            }

            // If amount is empty
            // and units non empty
            else if (units.nonEmpty) {
              if (units.get > 0.90 * holdingUnits) {
                retval = true
              }
              else
                retval = false
            }
            // If both amount and units are empty
            else {
              retval = true
            }
          })
        }
        else {
          // Send to BSE in case fhtRow is empty
          retval = true
          Future {}
        }
      })

      Await.result(a, Duration.Inf)
      retval
    }
    else {
      true
    }
  }
}
