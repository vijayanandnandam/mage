package service

import java.util.Calendar
import javax.inject.{Inject, Singleton}

import constants.{DBConstants, IntegrationConstants}
import helpers.integration.bse.BSEPasswordHelper
import models.integration.BSEUploadMfApiResponseValidateWrapper
import org.slf4j.LoggerFactory
import repository.module.{IntegrationRepository, PasswordRepository}
import repository.tables.FcdrqpRepo
import service.integration.BSEStarUploadServiceImpl
import utils.DateTimeUtils

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 21-04-2017.
  */

@Singleton
class PasswordService @Inject()(implicit ec: ExecutionContext,
                                passwordRepository: PasswordRepository,
                                bSEStarUploadServiceImpl: BSEStarUploadServiceImpl,
                                integrationRepository: IntegrationRepository,
                                fcdrqpRepo: FcdrqpRepo) extends IntegrationConstants with DBConstants{

  val logger, log = LoggerFactory.getLogger(classOf[PasswordService])

  /**
    *
    * @param bseApiPassCode
    * @return
    */
  def validateBSEEncryptedPassword(bseApiPassCode:String):Future[Int] = {

    bSEStarUploadServiceImpl.validateBSEAccountPassword().flatMap(validated =>{

      passwordRepository.getBSEEncryptedPassword(bseApiPassCode).flatMap(passRow => {

        val expiryTime = passRow._2
        val isValid = DateTimeUtils.checkBsePassTimeValidity(expiryTime)
        if(!isValid){
          passwordRepository.updateBSEPassword()
        } else{
          Future{0}
        }
      })
    })
  }



}
