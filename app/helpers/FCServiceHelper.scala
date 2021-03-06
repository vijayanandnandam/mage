package helpers

import javax.inject.{Inject, Singleton}

import constants.{BaseConstants, IntegrationConstants, KycConstants}
import models.integration.enumerations.BuySellEnum.BuySellEnum
import models.integration.enumerations.DPCEnum.DPCEnum
import models.integration.enumerations.EUINDeclarationEnum
import models.integration.enumerations.EUINDeclarationEnum.EUINDeclarationEnum
import org.slf4j.LoggerFactory
import repository.module.{IntegrationRepository, KycRepository}
import utils.bse.BSEUtility

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FCServiceHelper @Inject()(implicit ec: ExecutionContext, integrationRepository: IntegrationRepository,
                                kycRepository: KycRepository) extends IntegrationConstants with BaseConstants with KycConstants {

  val log, logger = LoggerFactory.getLogger(classOf[FCServiceHelper])

  def getBSEDefaultParameters(): Future[mutable.LinkedHashMap[String, String]] = {

    val bseDefaultParamList: ListBuffer[String] = ListBuffer[String](BSE_USER_ID_KEY, BSE_MEMBER_ID_KEY, BSE_PASSWORD_KEY,BSE_EUIN_KEY)

    integrationRepository.getDefaultParamValues(bseDefaultParamList, BSE_INTEGRATION_NAME)
  }


  def getBSEPassKey() = {

    BSEUtility.generatePassKey
  }


  def getFolioNo(buySell: BuySellEnum): Option[String] = {

    None
  }

  def getSubBrCode(): Option[String] = {
    None
  }

  def getSubBrARNCode(): Option[String] = {
    None
  }

  def getFolioNo(): Option[String] = {
    None
  }

  def getBrokerage(): Option[BigDecimal] = {
    None
  }

  def getEUIN(): Option[String] = {
    EUIN
  }

  def getOrderRemarks(): Option[String] = {
    None
  }

  def getEUINDeclaration(euin: Option[String]): EUINDeclarationEnum = {

    if (euin.isEmpty) {
      EUINDeclarationEnum.N
    } else {
      EUINDeclarationEnum.Y
    }
  }

  def getClientKYCStatus(clientCode: String): Future[String] = {

    kycRepository.getUserKYCStatus(clientCode.toLong).map(kycRowList => {
      if (kycRowList.isEmpty) {
        logger.error("Kyc Status for user pk " + clientCode + " not found")
        NO
      } else {
        val kycStatus = kycRowList.head.kycstatus
        if( kycStatus == KYC_DONE || kycStatus == KYC_EXTERNALLY_DONE){
          YES
        } else{
          NO
        }
      }
    })
  }

  def getDPC():DPCEnum = {
    DPC
  }
}