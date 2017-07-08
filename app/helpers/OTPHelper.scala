package helpers

import javax.inject.Singleton

import scala.util.Random

/**
  * Created by fincash on 13-02-2017.
  */

@Singleton
class OTPHelper {

  def generateOTP():Int = {

    new Random().nextInt(900000) + 100000
  }
}
