package helpers.integration.bse

import utils.bse.BSEUtility.PASS_KEY_LENGTH

import scala.util.Random

/**
  * Created by fincash on 24-05-2017.
  */
class BSEPasswordHelper {

  def generateBsePassword():String = {

    val specialCharString = "!@#$%^&*"
    val alphanumericString = Random.alphanumeric.take(7).mkString

    val password = new StringBuilder
    password.append(alphanumericString)

    val index = new Random().nextInt(8)
    password.append(specialCharString.charAt(index))

    password.toString
  }
}
