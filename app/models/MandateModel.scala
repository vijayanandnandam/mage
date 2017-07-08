package models

import java.sql.Timestamp

/**
  * Created by fincash on 18-02-2017.
  */
case class MandateModel(mandateBankRfNum:Long,amount:Double, mandateType:String, mandateAction:String, mandateId:String, mandateDate:Timestamp,
                        mandateFrequency:String, mandateDebitType:String, isuntilCancelled:String, deducteeName:String,
                        sotrfnum:Long, sotamount:Double)
