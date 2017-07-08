package service

import javax.inject.{Inject, Singleton}

import constants.DBConstants
import data.model.Tables.{FcmmtRow, FcmmtRowWrapper}
import models.{MandateModel, SubOrderDetails, UserBank}
import org.slf4j.LoggerFactory
import repository.module.{BankRepository, UserRepository}
import utils.DBConstantsUtil

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 18-02-2017.
  */

@Singleton
class BankService @Inject()(implicit ec: ExecutionContext, bankRepository: BankRepository, userRepository: UserRepository) extends DBConstants {

  val log, logger = LoggerFactory.getLogger(classOf[BankService])



  def saveMandate(mandate: MandateModel, userName: String) = {

    val mandateType = DBConstantsUtil.MANDATE_TYPE_MAP.getOrElse(mandate.mandateType, "")
    val fcmmtRow = new FcmmtRowWrapper(None, mandate.amount, Some(mandateType), mandate.mandateBankRfNum, None, None, Some(mandate.mandateDate),
      Some(mandate.mandateAction), Some(mandate.mandateFrequency), Some(mandate.mandateDebitType), Some(mandate.mandateDate),
      None, Some(mandate.isuntilCancelled), None, None, Some(mandate.mandateAction), BSE_DEDUCTEE_NAME, Some(mandate.mandateId)).get(userName)

    //val fcmmtlRow = new FcmmtlRowWrapper(None, -1, Some(mandate.sotrfnum)).get(userName)

    bankRepository.saveMandate(fcmmtRow)
  }

  def populateMandateUsageLog(sotRfnum: Long, existingMandate: String, orderAmount: Double, userName: String): Future[Any] = {
    bankRepository.insertToMandateLogTable(sotRfnum, existingMandate, orderAmount, userName)
  }

  def getMandateId(mmtrfnum: Long): Future[Option[(String,String)]] = {
    bankRepository.getMandateDetails(mmtrfnum).map(value => {
      if (value.isEmpty) {
        logger.error("Mandate Not found for " + mmtrfnum)
        Some("","")
      } else {
        Some(value.head.mmtumrn.get,value.head.mmtmandatetype.get)
      }
    })
  }
  def getMandate(subOrder:SubOrderDetails): Future[Option[FcmmtRow]] = {
    if(subOrder.investmentMode.toLowerCase=="sip"){
      if(subOrder.mandateDetails.nonEmpty){
        bankRepository.getMandateDetails(subOrder.mandateDetails.get.mmtrfnum).map(value => {
          if (value.isEmpty) {
            logger.error("Mandate Not found for " + subOrder.mandateDetails.get.mmtrfnum)
            None
          } else {
            Some(value.head)
          }
        })
      }else{   Future.apply(None) }
    }else{     Future.apply(None) }
  }

  def validateForMandateAmount(existingMandate: String, mandateAmount: Double): Future[Boolean] = {
    bankRepository.getAmountForMandateId(existingMandate).map(remainingAmount => {
      if (remainingAmount >= mandateAmount)
        true;
      else
        false;
    })
    //Future.apply(true);
  }

  def getMandateBankDetails(subOrder:SubOrderDetails):Future[Option[UserBank]] = {

    if(subOrder.investmentMode.toLowerCase=="sip"){
      if(subOrder.mandateDetails.nonEmpty){
        bankRepository.getMandateDetails(subOrder.mandateDetails.get.mmtrfnum).flatMap(mmtRows => {
          if (mmtRows.nonEmpty) {
            userRepository.getBankByMmmtbuarfum(mmtRows.head.mmtbuarfnum).map(orderBankDetails =>{
              Some(orderBankDetails)
            })
          }else{
            Future.apply(None)
          }
        })
      }else{Future.apply(None)}
    } else{
      Future.apply(None)
    }
  }
}
