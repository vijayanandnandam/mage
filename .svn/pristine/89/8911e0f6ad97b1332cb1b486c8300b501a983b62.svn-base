package service

import javax.inject.{Inject, Singleton}

import helpers.OTPHelper

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 13-02-2017.
  */

@Singleton
class OTPService @Inject()(implicit ec: ExecutionContext, oTPHelper: OTPHelper, userService: UserService) {

  def getOTP(purpose: String, otpServiceId: String, userId: Long): Int = {

    oTPHelper.generateOTP()

  }

  def saveUserOTPDetails(userId: Long, otp: Int, otpServiceId: String, purpose: String, mobileNo: String, ip: String): Future[(Long, Int)] = {

    userService.saveOTPDetails(userId, otp.toString, otpServiceId, purpose, mobileNo, ip).map(otpId => {
      (otpId, otp)
    })
  }

  def saveMessageId(messageId: String, otprfnum: Long, userName: String): Future[Int] = {

    userService.updateOTPMessageId(messageId, otprfnum, userName).map(value => value)
  }

  def validateUserOTP(otp: String, purpose: String, userId: Long): Future[(Boolean, String)] = {
    userService.validateOTP(otp, purpose, userId).map(otptRow => {

      if (otptRow.isDefined) {
        //removing country code from mobile number +91
        (true, otptRow.get.otptmobileno.substring(3))
      } else {
        (false, "")
      }
    })
  }

  def updateOTPStatus(messageId: String, status: String): Future[Int] = {
    userService.updateOTPStatus(messageId, status)
  }

  def getMessageStatus(messageId: String): Future[String] = {
    userService.getMessageStatus(messageId)
  }
}
