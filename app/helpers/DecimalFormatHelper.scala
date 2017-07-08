package helpers

import java.lang.Double

object DecimalFormat {
  
  def formatDouble(value:Double):Double = {
    
    Double.valueOf(new java.text.DecimalFormat("###.##").format(value))
  }
  
  def format3DecimalPlaces(value:Double):Double = {
    
    Double.valueOf(new java.text.DecimalFormat("###.###").format(value))
  }
  
  def formatBigDecimalToString(value:BigDecimal):String = {
    
    f"$value%.2f"
  }
  
  def formatDecimalPlace(value:BigDecimal):BigDecimal = {
    value.setScale(2, BigDecimal.RoundingMode.HALF_UP)
  }
}