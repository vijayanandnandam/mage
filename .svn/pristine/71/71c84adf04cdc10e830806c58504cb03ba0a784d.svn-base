package service.integration

import javax.inject.Inject

import models.FCOrderEntryModel
import models.integration.BSEOrderValidateWrapper
import models.integration.BSEUploadMfApiResponseValidateWrapper
import org.slf4j.LoggerFactory
import models.FCSipOrderEntryModel
import models.integration.BSESipOrderValidateWrapper
import models.FCXsipOrderEntryModel
import models.integration.BSEXsipOrderValidateWrapper
import models.FCSpreadOrderEntryModel
import models.integration.BSESpreadOrderValidateWrapper
import models.FCSwitchOrderEntryModel
import models.integration.BSESwitchOrderValidateWrapper
import models.integration.enumerations.FirstOrderEnum

import scala.concurrent.{ExecutionContext, Future}

class BSEOrderPaymentWrapper @Inject()(implicit ec:ExecutionContext, bseOrderEntryServiceImpl:BSEStarOrderEntryServiceImpl,
                                       bseUploadEntryServiceImpl:BSEStarUploadServiceImpl){
  
  val log = LoggerFactory.getLogger(classOf[BSEOrderPaymentWrapper])
  
  /**
   * Places the order and generates
   * the payment link
   */
  def placeOrderWithPayment(fcOrderEntry:FCOrderEntryModel,redirectUrl:String):Future[Boolean] = {
    
    bseOrderEntryServiceImpl.getOrderEntryParamResponse(fcOrderEntry,"test@test.com").map{bseOrderValidateWrapper =>
      val bseOrderResponseErrors = bseOrderValidateWrapper.errorList
      var success = true

      if(bseOrderResponseErrors.isEmpty){
        val bseOrderEntryResponse = bseOrderValidateWrapper.bseOrderEntryParamResponse
        bseUploadEntryServiceImpl.getPaymentGatewayResponse(bseOrderEntryResponse.clientCode, redirectUrl,fcOrderEntry.uniqueRefNo,"test@test.com").map{bseUploadMfApiResponseValidateWrapper =>
          if(!bseUploadMfApiResponseValidateWrapper.errorList.isEmpty){
            success = false
          }
        }

      } else{
        success = false
      }
      success
    }
    

  }
  
  /**
   * Places Sip Order and generates
   * the payment link
   */
  def placeSipOrderWithPayment(fcSipOrderEntry:FCSipOrderEntryModel,redirectUrl:String):Future[Boolean] = {
  
    bseOrderEntryServiceImpl.getSipOrderEntryParamResponse(fcSipOrderEntry,"test@test.com").map{bseSipOrderValidateWrapper =>
      val bseSipOrderResponseErrors = bseSipOrderValidateWrapper.errorList
      var success = true

      if(bseSipOrderResponseErrors.isEmpty){
        val bseSipOrderEntryResponse = bseSipOrderValidateWrapper.bseSipOrderEntryParamResponse
        bseUploadEntryServiceImpl.getPaymentGatewayResponse(bseSipOrderEntryResponse.clientCode, redirectUrl,fcSipOrderEntry.uniqueRefNo,"test@test.com").map{bseUploadMfApiResponseValidateWrapper =>
          if(!bseUploadMfApiResponseValidateWrapper.errorList.isEmpty){
            success = false
          }
        }

      } else{
        success = false
      }
      success
    }
  

  }
  
  /**
   * Places Xsip Order and generates
   * the payment link
   */
  def placeXsipOrderWithPayment(fcXsipOrderEntry:FCXsipOrderEntryModel,redirectUrl:String):Future[Boolean] = {
    
    bseOrderEntryServiceImpl.getXsipOrderEntryParamResponse(fcXsipOrderEntry,"test@test.com").map{bseXsipOrderValidateWrapper =>
      val bseXsipOrderResponseErrors = bseXsipOrderValidateWrapper.errorList
      var success = true

      if(fcXsipOrderEntry.firstOrderFlag == FirstOrderEnum.Y){
        if(bseXsipOrderResponseErrors.isEmpty){
          val bseXsipOrderEntryResponse = bseXsipOrderValidateWrapper.bseXsipOrderEntryParamResponse
          bseUploadEntryServiceImpl.getPaymentGatewayResponse(bseXsipOrderEntryResponse.clientCode, redirectUrl,fcXsipOrderEntry.uniqueRefNo,"test@test.com").map{bseUploadMfApiResponseVAlidateWrapper =>
            if(!bseUploadMfApiResponseVAlidateWrapper.errorList.isEmpty){
              success = false
            }
          }

        } else{
          success = false
        }
      }

      success
    }
    

  }
  
  /**
   * Places Spread Order and generates
   * the payment link
   */
  def placeSpreadOrderWithPayment(fcSpreadOrderEntry:FCSpreadOrderEntryModel,redirectUrl:String):Boolean = {
    /*val bseSpreadOrderValidateWrapper:BSESpreadOrderValidateWrapper = bseOrderEntryServiceImpl.getSpreadOrderEntryParamResponse(fcSpreadOrderEntry)
    
    val bseSpreadOrderResponseErrors = bseSpreadOrderValidateWrapper.errorList
    var success = true
    
    if(bseSpreadOrderResponseErrors.isEmpty){
      val bseSpreadOrderEntryResponse = bseSpreadOrderValidateWrapper.bseSpreadOrderEntryParamResponse
      val bseUploadMfApiResponseVAlidateWrapper:BSEUploadMfApiResponseValidateWrapper = bseUploadEntryServiceImpl.getPaymentGatewayResponse(bseSpreadOrderEntryResponse.clientCode, redirectUrl)
      if(!bseUploadMfApiResponseVAlidateWrapper.errorList.isEmpty){
        success = false
      }
    } else{
      success = false
    }
    success*/
    true
  }
  
  /**
   * Places Switch Order and generates
   * the payment link
   */
  /*def placeSwitchOrderWithPayment(fcSwitchOrderEntry:FCSwitchOrderEntryModel,redirectUrl:String):Boolean = {
    val bseSwitchOrderValidateWrapper:BSESwitchOrderValidateWrapper = bseOrderEntryServiceImpl.getSwitchOrderEntryParamResponse(fcSwitchOrderEntry)
    
    val bseSwitchOrderResponseErrors = bseSwitchOrderValidateWrapper.errorList
    var success = true
    
    if(bseSwitchOrderResponseErrors.isEmpty){
      val bseSwitchOrderEntryResponse = bseSwitchOrderValidateWrapper.bseSwitchOrderEntryParamResponse
      val bseUploadMfApiResponseVAlidateWrapper:BSEUploadMfApiResponseValidateWrapper = bseUploadEntryServiceImpl.getPaymentGatewayResponse(bseSwitchOrderEntryResponse.clientCode, redirectUrl)
      if(!bseUploadMfApiResponseVAlidateWrapper.errorList.isEmpty){
        success = false
      }
    } else{
      success = false
    }
    success
  }*/
}