package models.integration

import constants.IntegrationConstants

import scala.collection.mutable.HashMap

class BSEErrorModel {
  
}

object BSEErrorModel extends IntegrationConstants{
  
  val errorCodeMap = HashMap.empty[String,String]
  
  errorCodeMap += (ERROR_CODE_1 -> BSE_EMPTY_USER_ID_MSG)
  errorCodeMap += (ERROR_CODE_2 -> BSE_EMPTY_PASSWORD_MSG)
  errorCodeMap += (ERROR_CODE_3 -> BSE_EMPTY_PASSKEY_MSG)
  errorCodeMap += (ERROR_CODE_4 -> BSE_CAMS_IMG_VERIFY_MSG)
  errorCodeMap += (ERROR_CODE_5 -> BSE_DUPLICATE_REF_NO_MSG)
  errorCodeMap += (ERROR_CODE_6 -> BSE_DATA_TRUNC_MSG)
  errorCodeMap += (ERROR_CODE_7 -> BSE_MIN_ORDER_AMT_MSG)
  errorCodeMap += (ERROR_CODE_8 -> BSE_ORDER_NOT_ALLOWED_MSG)
  errorCodeMap += (ERROR_CODE_9 -> BSE_PASS_EXPIRE_MSG)
  errorCodeMap += (ERROR_CODE_10 -> INVALID_EUIN_MSG)
  errorCodeMap += (ERROR_CODE_11 -> MANDATE_NOT_APPROVE_MSG)
  errorCodeMap += (ERROR_CODE_12 -> FIRST_ORDER_MARKET_HOUR_MSG)
  errorCodeMap += (ERROR_CODE_13 -> XSIP_START_DATE_MSG)
  errorCodeMap += (ERROR_CODE_14 -> XSIP_NOT_ALLOWED_MSG)
  errorCodeMap += (ERROR_CODE_15 -> MIN_INSTALLMENT_MSG)
  errorCodeMap += (ERROR_CODE_16 -> INVALID_FIRST_ORDER_MSG)
  errorCodeMap += (ERROR_CODE_17 -> FUTURE_REDEEM_DATE_MSG)
  errorCodeMap += (ERROR_CODE_18 -> INVALID_PASS_KEY_MSG)
  errorCodeMap += (ERROR_CODE_19 -> XSIP_ALREADY_CANCELLED_MSG)
  errorCodeMap += (ERROR_CODE_20 -> START_DATE_MSG)
  errorCodeMap += (ERROR_CODE_21 -> CLIENT_NOT_EXISTS_MSG)
  errorCodeMap += (ERROR_CODE_22 -> CLIENT_CODE_BLANK_MSG)
  errorCodeMap += (ERROR_CODE_23 -> DIFF_RTA_SWITCH_MSG)
  errorCodeMap += (ERROR_CODE_24 -> SCHEME_NOT_FOUND_MSG)
  errorCodeMap += (ERROR_CODE_25 -> INSUFFICIENT_SO_AMT_MSG)
  errorCodeMap += (ERROR_CODE_26 -> KYC_NOT_DONE_MSG)
  errorCodeMap += (ERROR_CODE_27 -> INVALID_AMT_MSG)
  errorCodeMap += (ERROR_CODE_28 -> EMPTY_FLIO_NO_MSG)
  errorCodeMap += (ERROR_CODE_29 -> INVALID_ALL_REDEEM_MSG)
  errorCodeMap += (ERROR_CODE_30 -> INVALID_MIN_REDEEM_MSG)
  errorCodeMap += (ERROR_CODE_31 -> MAX_AMT_EXCEEDED_MSG)
  errorCodeMap += (ERROR_CODE_32 -> SWITCH_CANCEL_NOT_ALLOWED_MSG)
  errorCodeMap += (ERROR_CODE_33 -> MANDATORY_EUIN_MSG)
  errorCodeMap += (ERROR_CODE_34 -> INVALID_UPLOAD_STRING_MSG)
  errorCodeMap += (ERROR_CODE_35 -> INVALID_FLAG_MSG)
  errorCodeMap += (ERROR_CODE_36 -> PHYSICAL_MODE_NOT_ALLOWED_MSG)
  errorCodeMap += (ERROR_CODE_37 -> INVALID_EUIN_LENGTH_MSG)
  errorCodeMap += (ERROR_CODE_38 -> PURCHASE_NOT_ALLOWED_MSG)
  errorCodeMap += (ERROR_CODE_39 -> MANDATE_EXHAUSTED_MSG)
  errorCodeMap += (ERROR_CODE_40 -> XSIP_NOT_ALLOWED_IN_AMC_MSG)
  errorCodeMap += (ERROR_CODE_41 -> INVALID_MANDATE_ID_MSG)
  errorCodeMap += (ERROR_CODE_42 -> BOTH_MANDATE_ID_BLANK_MSG)
  errorCodeMap += (ERROR_CODE_43 -> SWITCH_IN_DIFF_AMC_NOT_ALLOWED_MSG)
  errorCodeMap += (ERROR_CODE_44 -> XSIP_MAX_START_DATE_GAP_MSG)
  errorCodeMap += (ERROR_CODE_45 -> XSIP_MIN_CANCEL_DAYS_MSG)
  errorCodeMap += (ERROR_CODE_46 -> INVALID_SEGMENT_VALUE_MSG)
  errorCodeMap += (ERROR_CODE_47 -> INVALID_ORDER_NUMBER_MSG)
  errorCodeMap += (ERROR_CODE_48 -> INVALID_ORDER_NUM_FOR_MEMBER_MSG)
  errorCodeMap += (ERROR_CODE_49 -> INVALID_ORDER_NUM_FOR_CLIENT_MSG)
  
  def getErrorCode(errorMessage:String):(String,String) = {
    
    var errorMsg = GENERAL_ERROR_MSG
    var errorCode = GENERAL_ERROR_CODE
  
    val newErrorMessage = errorMessage.trim.toLowerCase
    
    for((k,v) <- errorCodeMap){
      if(newErrorMessage.indexOf(v.toLowerCase) != -1){
        errorCode = k
        errorMsg = v
        return (errorCode,errorMsg)
      }
    }
    
    (errorCode,errorMsg)
  }
  
}