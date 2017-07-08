package validator

import constants.IntegrationConstants
import models.ErrorModel
import models.integration.{BSEErrorModel, BSESpreadOrderEntryParamResponse}
import org.slf4j.LoggerFactory
import service.PropertiesLoaderService

import scala.collection.mutable.ListBuffer

class BSESpreadOrderResponseValidator {
  
}

object BSESpreadOrderResponseValidator extends BaseValidator[BSESpreadOrderEntryParamResponse] with IntegrationConstants{
  val log, logger = LoggerFactory.getLogger(getClass)

  override def validate(bseSpreadOrderEntryParamResponse:BSESpreadOrderEntryParamResponse):ListBuffer[ErrorModel] = {
    
    val errorsList:ListBuffer[ErrorModel] = ListBuffer.empty[ErrorModel]
    val successFlag = bseSpreadOrderEntryParamResponse.successFlag
    val remarks = bseSpreadOrderEntryParamResponse.bseRemarks.getOrElse("")
    if(successFlag != BSE_SUCCESS_FLAG_0){

      logger.debug("BSE Spread order Error Occured")
      val errorTuple = BSEErrorModel.getErrorCode(remarks)
      val errorCode = errorTuple._1
      val errorMessage = errorTuple._2
      
      var errorModel = ErrorModel(errorCode,errorMessage)
      
      if(errorCode == GENERAL_ERROR_CODE){
        errorModel = ErrorModel(SPREAD_ERROR_CODE_104,PropertiesLoaderService.getConfig().getString("bse.spreadOrderEntry.response.errorMessage"))
      }
    	errorsList.+=:(errorModel)
    	
    }
    errorsList
  }
}