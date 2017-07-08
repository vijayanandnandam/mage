package validator

import service.PropertiesLoaderService
import constants.IntegrationConstants
import models.ErrorModel
import models.integration.BSEErrorModel
import models.integration.BSEGetPasswordResponse
import scala.collection.mutable.ListBuffer

class BSEPasswordResponseValidator {
  
}

object BSEPasswordResponseValidator extends BaseValidator[BSEGetPasswordResponse] with IntegrationConstants{
  
  override def validate(getBSEPasswordResponse:BSEGetPasswordResponse):ListBuffer[ErrorModel] = {
    
    val errorsList:ListBuffer[ErrorModel] = ListBuffer.empty[ErrorModel]
    val responseCode = getBSEPasswordResponse.responseCode
    val encryptedPassword = getBSEPasswordResponse.encryptedPassword
    if(responseCode != BSE_RESPONSE_CODE_100){
      
      val errorTuple = BSEErrorModel.getErrorCode(encryptedPassword)
      val errorCode = errorTuple._1
      val errorMessage = errorTuple._2
      
      var errorModel = ErrorModel(errorCode,errorMessage)
      
      if(errorCode == GENERAL_ERROR_CODE){
        errorModel = ErrorModel(PASSWORD_ERROR_CODE_101,PropertiesLoaderService.getConfig().getString("bse.getPassword.response.errorMessage"))
      }
      
      errorsList.+=:(errorModel)
    }
    
    errorsList
  }
}