package constants

/**
  * Created by Fincash on 12-05-2017.
  */
trait ZendeskConstants extends  CNDConstants{
  final val ZENDESK_TICKET_SUBJECT = 42639549L
  final val ZENDESK_TICKET_DESCRIPTION = 42639569L
  final val ZENDESK_TICKET_STATUS = 42639589L
  final val ZENDESK_TICKET_TICKET_TYPE = 42639609L
  final val ZENDESK_TICKET_PRIORITY = 42639629L
  final val ZENDESK_TICKET_GROUP = 42639649L
  final val ZENDESK_TICKET_ASSIGNEE = 42639669L
  final val ZENDESK_TICKET_USER_STAGE = 44939569L
  final val ZENDESK_TICKET_KYC_STATUS = 45002045L
  final val ZENDESK_TICKET_ORDER_STATUS = 45212969L
  final val ZENDESK_TICKET_PAYMENT_LINK = 45212989L


  final val ZENDESK_TICKET_VALUE_ACCOUNT_ACTIVE = "account_active"
  final val ZENDESK_TICKET_VALUE_DOCUMENTS_APPROVED = "documents_approved"
  final val ZENDESK_TICKET_VALUE_DOCUMENTS_PENDING = "documents_pending"
  final val ZENDESK_TICKET_VALUE_MOBILE_VERIFIED = "mobile_verified"
  final val ZENDESK_TICKET_VALUE_PAN_UPDATED = "pan_updated"
  final val ZENDESK_TICKET_VALUE_REGISTRATION_COMPLETE = "registration_complete"
  final val ZENDESK_TICKET_VALUE_REGISTRATION_FAILED = "registration_failed"
  final val ZENDESK_TICKET_VALUE_REGISTRATION_INCOMPLETE = "registration_incomplete"
  final val ZENDESK_TICKET_VALUE_SIGN_UP = "sign_up"
  final val ZENDESK_TICKET_VALUE_AADHAAR_KYC_INITIATED = "aadhaar_kyc_initiated"

  final val ZENDESK_TICKET_VALUE_AADHAAR_EKYC = "aadhaar_ekyc"
  final val ZENDESK_TICKET_VALUE_EKYC_FAILED = "ekyc_failed"
  final val ZENDESK_TICKET_VALUE_KYC_NOT_DONE = "kyc_not_done"
  final val ZENDESK_TICKET_VALUE_KYC_DONE = "kyc_done"
  final val ZENDESK_TICKET_VALUE_KYC_UNDER_PROCESS = "kyc_under_process"
  final val ZENDESK_TICKET_VALUE_KYC_DONE_EKYC = "ekyc"

  final val ZENDESK_TICKET_VALUE_KYC_DONE_BY_FINCASH = "kyc_done_fincash"

  final val ZENDESK_USER_FIELD_NAME_PAN = "pan"
  final val ZENDESK_USER_FIELD_NAME_DOB = "dob"
  final val ZENDESK_USER_FIELD_NAME_KYC = "kyc"
  final val ZENDESK_USER_FIELD_NAME_BANK = "bank"
  final val ZENDESK_USER_FIELD_NAME_USER_ID = "user_id"

  final val ZENDESK_GROUP_ONBOARDING = 28448865L
  final val ZENDESK_GROUP_PURCHASE = 28389269L
  final val ZENDESK_GROUP_REDEMPTION = 28448905L
  final val ZENDESK_GROUP_SUPPORT = 27940229L
  final val ZENDESK_GROUP_SYSTEMATIC_INVESTMENT = 28448925L

  final val ZENDESK_SOURCE_API = "API"
  final val ZENDESK_SOURCE_EMAIL = "email"
  final val ZENDESK_SOURCE_CHAT = "chat"
}

/*trait ZendeskTicketFieldValueConstant{


}
trait ZendeskUserFieldConstant {

}

trait ZendeskUserFieldValueConstant{


}
trait ZendeskGroupConstants {

}
trait ZendesktktSourceConstants {

}*/
