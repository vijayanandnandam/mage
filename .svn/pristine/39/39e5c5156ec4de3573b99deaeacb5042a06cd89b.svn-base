package helpers

import com.fincash.integration.ws.client.bsestar.upload.{GetPasswordResponse, MFAPIResponse}
import constants.IntegrationConstants
import models.ErrorModel
import models.integration.{BSEGetPasswordResponse, BSEPasswordValidateWrapper, BSEUploadMfApiResponse, BSEUploadMfApiResponseValidateWrapper}
import org.slf4j.LoggerFactory
import validator.{BSEMfApiResponseValidatorImpl, BSEPasswordResponseValidator, BSEPaymentStatusResponseValidatorImpl}

import scala.collection.mutable.ListBuffer

class BSEUploadResponseHelper {
  
}

object BSEUploadResponseHelper extends IntegrationConstants{
  val logger , log = LoggerFactory.getLogger(getClass)


  def convertBSEResponse(getPasswordResponse:GetPasswordResponse):BSEPasswordValidateWrapper = {
    
    val responseString = getPasswordResponse.getGetPasswordResult.getValue
    var responseValues:Array[String] = responseString.split('|')
    
    if(responseString.endsWith(PIPE_SEPARATOR)){
      responseValues = responseValues :+ ""
    }
    
    val bseGetPasswordResponse = BSEGetPasswordResponse(responseValues(0),responseValues(1).trim)
    
    val errors:ListBuffer[ErrorModel] = BSEPasswordResponseValidator.validate(bseGetPasswordResponse)

    val bsePasswordValidateWrapper = BSEPasswordValidateWrapper(bseGetPasswordResponse,Some(errors))
    bsePasswordValidateWrapper
  }
  
  def convertBSEResponse(mfApiResponse:MFAPIResponse,referenceNumberRequired:Boolean):BSEUploadMfApiResponseValidateWrapper = {

    logger.debug("Parsing BSE MfApi Response")
    val responseString = mfApiResponse.getMFAPIResult.getValue
    var responseValues:Array[String] = responseString.split('|')
    
    if(responseString.endsWith(PIPE_SEPARATOR)){
      responseValues = responseValues :+ ""
    }
    
    var bseMfApiResponse = BSEUploadMfApiResponse(responseValues(0),responseValues(1),None)
    
    val errors:ListBuffer[ErrorModel] = BSEMfApiResponseValidatorImpl.validate(bseMfApiResponse)
    
    if(referenceNumberRequired && errors.isEmpty){
      bseMfApiResponse = bseMfApiResponse.copy(referenceNumber = Some(responseValues(2)))
    }
    
    val bseMfApiResponseValidateWrapper = BSEUploadMfApiResponseValidateWrapper(bseMfApiResponse,Some(errors))
    
    bseMfApiResponseValidateWrapper
  }
  
  def convertPaymentStatusResponse(mfApiResponse:MFAPIResponse):BSEUploadMfApiResponseValidateWrapper = {

    logger.debug("Parsing BSE Payment Status Response")
    val responseString = mfApiResponse.getMFAPIResult.getValue
    var responseValues:Array[String] = responseString.split('|')
    
    if(responseString.endsWith(PIPE_SEPARATOR)){
      responseValues = responseValues :+ ""
    }
    
    var bseMfApiResponse = BSEUploadMfApiResponse(responseValues(0),responseValues(1),None)
    
    val errors:ListBuffer[ErrorModel] = BSEPaymentStatusResponseValidatorImpl.validate(bseMfApiResponse)
    
    val bseMfApiResponseValidateWrapper = BSEUploadMfApiResponseValidateWrapper(bseMfApiResponse,Some(errors))
    
    bseMfApiResponseValidateWrapper
  }
}