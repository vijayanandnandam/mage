package validator

import constants.IntegrationConstants
import models.ErrorModel
import models.integration.{BSEErrorModel, BSESwitchOrderEntryParamResponse}
import org.slf4j.LoggerFactory
import service.PropertiesLoaderService

import scala.collection.mutable.ListBuffer

class BSESwitchOrderResponseValidator {
  
}

object BSESwitchOrderResponseValidator extends BaseValidator[BSESwitchOrderEntryParamResponse] with IntegrationConstants{

  val log, logger = LoggerFactory.getLogger(getClass)


  override def validate(bseSwitchOrderEntryParamResponse:BSESwitchOrderEntryParamResponse):ListBuffer[ErrorModel] = {
    
    val errorsList:ListBuffer[ErrorModel] = ListBuffer.empty[ErrorModel]
    val successFlag = bseSwitchOrderEntryParamResponse.successFlag
    val remarks = bseSwitchOrderEntryParamResponse.bseRemarks.getOrElse("")
    
    if(successFlag != BSE_SUCCESS_FLAG_0){

      logger.debug("BSE Switch order Error Occured")
      val errorTuple = BSEErrorModel.getErrorCode(remarks)
      val errorCode = errorTuple._1
      val errorMessage = errorTuple._2
      
      var errorModel = ErrorModel(errorCode,errorMessage)
      
      if(errorCode == GENERAL_ERROR_CODE){
        errorModel = ErrorModel(SWITCH_ERROR_CODE_105,PropertiesLoaderService.getConfig().getString("bse.switchOrderEntry.response.errorMessage"))
      }
    	errorsList.+=:(errorModel)
    	
    }
    errorsList
  }
}