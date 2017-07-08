package validator

import constants.IntegrationConstants
import models.ErrorModel
import models.integration.{BSEErrorModel, BSEUploadMfApiResponse}
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer

object BSEPaymentStatusResponseValidatorImpl extends BaseValidator[BSEUploadMfApiResponse] with IntegrationConstants{

  val log, logger = LoggerFactory.getLogger(getClass)


  override def validate(bseUploadMfApiResponse:BSEUploadMfApiResponse):ListBuffer[ErrorModel] = {
    
    val errorsList:ListBuffer[ErrorModel] = ListBuffer.empty[ErrorModel]
    val status = bseUploadMfApiResponse.status
    val response = bseUploadMfApiResponse.response
    if(status != BSE_RESPONSE_CODE_100){
      logger.debug("Error Occured in Mf Api Payment Response")
      val errorTuple = BSEErrorModel.getErrorCode(response)
      val errorCode = errorTuple._1
      val errorMessage = errorTuple._2
      
      val errorModel = ErrorModel(errorCode,errorMessage)
      
      errorsList.+=:(errorModel)
    } /*else{
      if(response.indexOf(NEFT_RTGS_PAYMENT_APPROVED_MSG.toLowerCase) == -1 
          && response.indexOf(DIRECT_PAYMENT_APPROVED_MSG.toLowerCase) == -1){
        logger.debug("Error Occured in Mf Api Payment Response")
        val errorCode = PAYMENT_ERROR_CODE_107
        val errorMessage = response
        
        val errorModel = ErrorModel(errorCode,errorMessage)
        
        errorsList.+=:(errorModel)
      }
    }*/
    errorsList
  }
}