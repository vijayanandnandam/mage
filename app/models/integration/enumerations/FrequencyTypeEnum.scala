package models.integration.enumerations

object FrequencyTypeEnum extends Enumeration{
  
  type FrequencyTypeEnum = Value
  
  val SEMI_ANNUALLY = Value(1,"SEMI-ANNUALLY")
  
  val MONTHLY,QUATERLY,WEEKLY,ANNUALLY= Value
}