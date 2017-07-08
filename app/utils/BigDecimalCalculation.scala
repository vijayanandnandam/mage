package utils

import scala.math.BigDecimal
import scala.math.BigDecimal.RoundingMode
import scala.math.BigDecimal.RoundingMode.RoundingMode
/**
  * Created by Fincash on 24-05-2017.
  */
object Calculation {
  val DECIMALS_0 = 0
  val DECIMALS_1 = 1
  val DECIMALS_2 = 2
  val DECIMALS_3 = 3
  val DECIMALS = 4
  val DECIMALS_GENERAL = 10

  def rounded(aNumber: BigDecimal): BigDecimal = {
    aNumber.setScale(DECIMALS, BigDecimal.RoundingMode.HALF_DOWN)
  }

  def rounded(aNumber: BigDecimal, roundingMode : RoundingMode, decimal : Int): BigDecimal = {
    aNumber.setScale(decimal, roundingMode)
  }

  def  roundedUp(aNumber: BigDecimal): BigDecimal = {
    aNumber.setScale(DECIMALS, RoundingMode.UP)
  }

  def  roundedHalfUp(aNumber: BigDecimal): BigDecimal = {
    aNumber.setScale(DECIMALS, RoundingMode.HALF_UP)
  }

  def  roundedHalfEven(aNumber: BigDecimal): BigDecimal = {
    aNumber.setScale(DECIMALS, RoundingMode.HALF_EVEN)
  }

  def  roundedHalfDown(aNumber: BigDecimal): BigDecimal = {
    aNumber.setScale(DECIMALS, RoundingMode.HALF_DOWN)
  }

  def  multiplyRounded( _number1: BigDecimal,  _number2: BigDecimal): BigDecimal = {
    rounded(_number1.*(_number2))
  }

  def  multiply( _number1: BigDecimal,  _number2: BigDecimal): BigDecimal = {
    _number1.*(_number2)
  }

  def  divideRounded( _number1: BigDecimal,  _number2: BigDecimal): BigDecimal = {
    rounded(_number1./(_number2), RoundingMode.HALF_DOWN, DECIMALS)
  }

  def  divide( _number1: BigDecimal,  _number2: BigDecimal): BigDecimal = {
    _number1./(_number2)
  }

  def  addRounded( _number1: BigDecimal,  _number2: BigDecimal): BigDecimal = {
    rounded(_number1.+(_number2), RoundingMode.HALF_DOWN, DECIMALS)
  }

  def  add( _number1: BigDecimal,  _number2: BigDecimal): BigDecimal = {
    _number1.+(_number2)
  }

  def subRounded( _number1: BigDecimal,  _number2: BigDecimal): BigDecimal = {
    rounded(_number1.-(_number2), RoundingMode.HALF_DOWN, DECIMALS)
  }

  def  sub( _number1: BigDecimal,  _number2: BigDecimal): BigDecimal = {
    _number1.-(_number2)
  }

  def  percentageRounded( _number1: BigDecimal,  _number2: BigDecimal): BigDecimal = {
    var result = _number1.*(_number2)
    result = result./(100)
    rounded(result, RoundingMode.HALF_DOWN, DECIMALS)
  }

  def  percentage( _number1: BigDecimal,  _number2: BigDecimal): BigDecimal = {
    var result = _number1.*(_number2)
    result./(100)
  }

  def createRounded(_number1: Double): BigDecimal = {
    val number1 = BigDecimal(_number1)
    rounded(number1, RoundingMode.HALF_DOWN, DECIMALS)
  }

  def  create(_number1: Double): BigDecimal = {
     BigDecimal(_number1)
  }

  def isEqual( _number1: BigDecimal,  _number2: BigDecimal): Boolean = {
    _number1.equals(_number2)
  }

  /*def  stripTrailingZeros( input) {
    return input == null?input:input.stripTrailingZeros();
  }*/
}
