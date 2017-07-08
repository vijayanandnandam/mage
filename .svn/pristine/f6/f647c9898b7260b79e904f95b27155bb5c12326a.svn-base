package service

import javax.inject.Inject

import helpers.OTPHelper

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 13-02-2017.
  */
class OTPService @Inject()(implicit ec:ExecutionContext, oTPHelper: OTPHelper, userService: UserService) {

  def getOTP(purpose:String,otpServiceId:String,userId:Long):Future[(Long,Int)] = {

    val otp = oTPHelper.generateOTP()

    userService.saveOTPDetails(userId,otp.toString,otpServiceId,purpose).map(otpId =>{
      (otpId,otp)
    })
  }

  def saveMessageId(messageId:String,otprfnum:Long,userName:String):Future[Int] = {

    userService.updateOTPMessageId(messageId,otprfnum,userName).map(value => value)
  }

  def validateOTP(otp:String,purpose:String,userId:Long):Future[Boolean] = {
    userService.validateOTP(otp,purpose,userId).map(value => {
      if(value == 1){
        true
      } else{
        false
      }
    })
  }

  def updateOTPStatus(messageId:String,status:String):Future[Int] = {
    userService.updateOTPStatus(messageId,status)
  }
  def getMessageStatus(messageId:String):Future[String] = {
    userService.getMessageStatus(messageId)
  }
}
