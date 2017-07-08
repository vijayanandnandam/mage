package service

import javax.inject.Inject

import constants.DBConstants
import data.model.Tables.FcmmtRowWrapper
import models.MandateModel
import org.slf4j.LoggerFactory
import repository.module.BankRepository
import utils.DBConstantsUtil

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 18-02-2017.
  */
class BankService @Inject()(implicit ec: ExecutionContext, bankRepository: BankRepository) extends DBConstants {

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

  def validateForMandateAmount(existingMandate: String, mandateAmount: Double): Future[Boolean] = {
    bankRepository.getAmountForMandateId(existingMandate).map(remainingAmount => {
      if (remainingAmount >= mandateAmount)
        true;
      else
        false;
    })
    //Future.apply(true);
  }
}
