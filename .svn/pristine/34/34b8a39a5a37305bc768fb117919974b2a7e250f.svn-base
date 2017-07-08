package service.integration

import javax.inject.Inject

import constants.IntegrationConstants
import data.model.Tables.{FcomtRow, FcsotRow}
import models.{OrderModel, ProcessedSubOrderModel, UserLoginObject}
import org.slf4j.LoggerFactory
import repository.module.IntegrationRepository
import service.SchemeService

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}


/**
  * Created by fincash on 19-04-2017.
  */
class IntegrationServiceImpl @Inject()(implicit ec: ExecutionContext,
                                       bSEIntegrationServiceImpl: BSEIntegrationServiceImpl,
                                       integrationRepository: IntegrationRepository, schemeService: SchemeService) extends IntegrationConstants {

  val logger, log = LoggerFactory.getLogger(classOf[IntegrationServiceImpl])


  val integrationMap: mutable.HashMap[Long, IntegrationService] = new mutable.HashMap[Long, IntegrationService]()
  integrationMap.+=(BSE_INTEGRATION_VALUE -> bSEIntegrationServiceImpl)


  def placeSubOrders(orderModel: OrderModel, order: FcomtRow, subOrderList: List[FcsotRow], userLoginObject: UserLoginObject): Future[List[ProcessedSubOrderModel]] = {

    val subOrderProcessedListFtr = for (subOrder <- subOrderList) yield {
      schemeService.getSchemeIdByOptionId(subOrder.sotsoptrfnum.toString).flatMap(smtrfnum => {

        integrationRepository.getIntegration(smtrfnum, orderModel.orderType).flatMap(integrationTypeList => {
          var integrationType = BSE_INTEGRATION_VALUE
          if (integrationTypeList.nonEmpty) {
            integrationType = integrationTypeList.head.siaimtrfnum
          }
          integrationMap.getOrElse(integrationType, bSEIntegrationServiceImpl).placeSubOrders(orderModel, order, List(subOrder), userLoginObject)
        })
      })
    }

    Future.sequence(subOrderProcessedListFtr).map(subOrderPtocessLists => {
      subOrderPtocessLists.flatten
    })
  }
}
