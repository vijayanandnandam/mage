package utils

import java.util.Locale
import java.util.Currency

object NumberUtils {
  
  def convertToCurrencyFormat(number:BigDecimal):String = {
    
    val formatter = java.text.NumberFormat.getCurrencyInstance()
    val in = Currency.getInstance(new Locale("en", "in"))
    formatter.setCurrency(in)
    
    formatter.format(number).replace("Rs.", "")
    
  }
  
  def convertToIndianCurrency(number:BigDecimal):String = {
    
    val formatter = java.text.NumberFormat.getCurrencyInstance()
    val in = Currency.getInstance(new Locale("en", "in"))
    formatter.setCurrency(in)
    
    formatter.format(number).replace("Rs.", "\u20B9")
  }
}