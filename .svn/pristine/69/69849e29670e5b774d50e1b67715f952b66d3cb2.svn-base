package utils.bse

import utils.DateTimeUtils

import scala.util.Random
import constants.{DBConstants, IntegrationConstants}

import scala.collection.mutable.ListBuffer

object BSEUtility extends IntegrationConstants with DBConstants{
  
  /**
   * Generates the unique
   * reference number 
   */
  def getUniqueReferenceNumber(uniqueNumber:String):String = {
    
    val currentDate = DateTimeUtils.getDateInYYYYMMDD
    val uniqueRefNo:StringBuilder = new StringBuilder
    
    uniqueRefNo.append(currentDate)
    uniqueRefNo.append(uniqueNumber)
     
    uniqueRefNo.toString
  }
  
  /**
   * Generates random
   * alphanumeric string
   */
  def generatePassKey():String = {
    
    Random.alphanumeric.take(PASS_KEY_LENGTH).mkString
  }
  
  def getPaymentGatewayFlag():String = {
    
    BSE_MF_API_PAYMENT_FLAG
  }
  
  def getFatcaUploadFlag():String = {
    
    BSE_MF_API_FATCA_UPLOAD_FLAG
  }
  
  def getUCCFlag():String = {
    
    BSE_MF_API_UCC_FLAG
  }
  
  def getMandateFlag():String = {
    
    BSE_MF_API_MANDATE_FLAG
  }
  
  def getSTPFlag():String = {
    
    BSE_MF_API_STP_FLAG
  }
  
  def getSWPFlag():String = {
    
    BSE_MF_API_SWP_FLAG
  }
  def getChangePasswordFlag():String = {
    
    BSE_MF_API_CHANGE_PASSWORD_FLAG
  }
  
  def getOrderPaymentStatusFlag():String = {
    BSE_CLIENT_ORDER_PAYMENT_STATUS_FLAG
  }
  
  /**
   * Appends all the parameters
   * with pipe separator
   */
  def formatValue(params:Any*):String = {
    
    val param:StringBuilder = new StringBuilder
    param.append(params(0))
    
    for(i <- 1 until params.length){
      param.append(PIPE_SEPARATOR)
      param.append(params(i))
    }
    
    param.toString
  }

  /**
    * Appends all the parameters
    * with pipe separator
    */
  def formatValuesList(paramsList:ListBuffer[String]) = {

    val param:StringBuilder = new StringBuilder
    param.append(paramsList(0))

    for(i <- 1 until paramsList.length){
      param.append(PIPE_SEPARATOR)
      param.append(paramsList(i))
    }

    param.toString
  }
  
  /**
   * Generates pipe separated
   * format of password
   */
  def generateChangePasswordParam(currPassword:String,newPassword:String):String = {
    
    val passwordParam:StringBuilder = new StringBuilder
    passwordParam.append(currPassword).append(PIPE_SEPARATOR)
                  .append(newPassword).append(PIPE_SEPARATOR)
                  .append(newPassword)
    
    passwordParam.toString
  }


  def convertToBSEAddress(address1:String,address2:String):(String,String,String) = {

    val addressField1 = address1
    var addressField2 = address2
    var addressField3 = ""

    if(address2.length > 40){
      val addresssWords = addressField2.split(Array(' ',',','-'))
      var currentWord = ""
      for(word <- addresssWords){
        if((currentWord + word).length <= 40){
          currentWord = currentWord + word
        } else{
          addressField2 = currentWord.trim
          currentWord = ""
        }
        currentWord = currentWord + " "
      }
      addressField3 = currentWord.trim
    }

    (addressField1,addressField2,addressField3)
  }

  def getClientName(firstName:String,middleName:Option[String],lastName:Option[String]) = {

    var clientName = firstName
    if(middleName.isDefined){
      clientName = clientName + middleName.get
    }
    if(lastName.isDefined){
      clientName = clientName + lastName.get
    }
    clientName
  }

  def getGender(gender:String):String = {

    if(gender == OTHER){
      MALE
    } else{
      gender
    }
  }

}