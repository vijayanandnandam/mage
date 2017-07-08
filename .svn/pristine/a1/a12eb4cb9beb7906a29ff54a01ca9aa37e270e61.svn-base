package validator

import constants.IntegrationConstants
import models.ErrorModel
import models.integration.{BSEErrorModel, BSEOrderEntryParamResponse}
import org.slf4j.LoggerFactory
import service.PropertiesLoaderService

import scala.collection.mutable.ListBuffer

class BSEOrderResponseValidator {
  
}

object BSEOrderResponseValidator extends BaseValidator[BSEOrderEntryParamResponse] with IntegrationConstants{

  val log, logger = LoggerFactory.getLogger(getClass)


  override def validate(bseOrderEntryParamResponse:BSEOrderEntryParamResponse):ListBuffer[ErrorModel] = {
    
    val errorsList:ListBuffer[ErrorModel] = ListBuffer.empty[ErrorModel]
    val remarks = bseOrderEntryParamResponse.bseRemarks.getOrElse("")
    val successFlag = bseOrderEntryParamResponse.successFlag
    
    if(successFlag != BSE_SUCCESS_FLAG_0){


      val errorTuple = BSEErrorModel.getErrorCode(remarks)
      val errorCode = errorTuple._1
      val errorMessage = errorTuple._2
      
      var errorModel = ErrorModel(errorCode,errorMessage)
      logger.debug("BSE Order Error Occured " + remarks)
      if(errorCode == GENERAL_ERROR_CODE){
        errorModel = ErrorModel(ORDER_ERROR_CODE_102,PropertiesLoaderService.getConfig().getString("bse.orderEntry.response.errorMessage"))
      }
    	errorsList.+=:(errorModel)
    }
    errorsList
  } 
}