package validator

import service.PropertiesLoaderService
import constants.IntegrationConstants
import models.ErrorModel
import models.integration.BSEErrorModel
import models.integration.BSESipOrderEntryParamResponse
import org.slf4j.LoggerFactory
import play.api.Logger

import scala.collection.mutable.ListBuffer

class BSESipOrderResponseValidator {
  
}

object BSESipOrderResponseValidator extends BaseValidator[BSESipOrderEntryParamResponse] with IntegrationConstants{

  val log, logger = LoggerFactory.getLogger(getClass)


  override def validate(bseSipOrderEntryParamResponse:BSESipOrderEntryParamResponse):ListBuffer[ErrorModel] = {
    
    val errorsList:ListBuffer[ErrorModel] = ListBuffer.empty[ErrorModel]
    val successFlag = bseSipOrderEntryParamResponse.successFlag
    val remarks = bseSipOrderEntryParamResponse.bseRemarks.getOrElse("")
    if(successFlag != BSE_SUCCESS_FLAG_0){

      logger.debug("BSE Sip order Error Occured")
      val errorTuple = BSEErrorModel.getErrorCode(remarks)
      val errorCode = errorTuple._1
      val errorMessage = errorTuple._2
      
      var errorModel = ErrorModel(errorCode,errorMessage)
      
      if(errorCode == GENERAL_ERROR_CODE){
        errorModel = ErrorModel(SIP_ERROR_CODE_103,PropertiesLoaderService.getConfig().getString("bse.sipOrderEntry.response.errorMessage"))
      }
    	errorsList.+=:(errorModel)

    }
    errorsList
  }
}