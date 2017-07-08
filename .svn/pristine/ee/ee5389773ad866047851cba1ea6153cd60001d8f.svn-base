package models

import play.api.libs.json._


case class Checkout(checkoutId: String,
                    fundsData: List[CheckoutFund],
                    nomineeList: Option[List[CheckoutNominee]],
                    bankList: List[CheckoutBank],
                    selectedBank: Option[CheckoutBank] = None,
                    selectedNominee: Option[CheckoutNominee] = None,
                    totalInvestmentAmount: Option[Long] = None)

case class CheckoutFund(fund: FundDoc,
                        options: List[FundOption],
                        selectedPlan: Option[String] = None,
                        selectedOption: Option[FundOption] = None,
                        selectedInvestmentMode: Option[String] = None,
                        selectedAIPFrequency: Option[String] = None,
                        selectedAIPDate: Option[Long] = None,
                        noOfInstallment: Option[Long] = None,
                        amount: Option[Long] = None)

case class CheckoutNominee(id: Option[Long] = None,
                           name: Option[String] = None,
                           relation: Option[String] = None)

case class CheckoutBank(id: Option[Long] = None,
                        bankName: Option[String] = None,
                        branchName: Option[String] = None,
                        accNumber: Option[String] = None,
                        IFSCCode: Option[String] = None)


case class SummaryFund(soptRfnum: Long,
                       name: String,
                       plan: String,
                       option: String,
                       investmentMode: String,
                       aIPFrequency: Option[String],
                       deductionDate: Option[Long],
                       noOfInstallment: Option[Long],
                       amount: Long)


case class Summary(fundList: Seq[SummaryFund],
                   bankRfnum: Long,
                   totalAmount: Long,
                   imagePath: String);

case class SummaryUserInfo(name: String,
                           pan: String,
                           ip: String,
                           time: String);

case class PaymentObject(orderId: Long,
                         paymentUrl: String,
                         bseUrl: Option[String]);


object CheckoutJsonFormats {

  import models.FundsJsonFormats._

  implicit val checkoutNomineeFormat = Json.format[CheckoutNominee]
  implicit val checkoutBankFormat = Json.format[CheckoutBank]
  implicit val checkoutFundFormat = Json.format[CheckoutFund]
  implicit val checkoutFormat = Json.format[Checkout]

  implicit val summaryUserInfoFormat = Json.format[SummaryUserInfo]
  implicit val summaryFundFormat = Json.format[SummaryFund]
  implicit val summaryFormat = Json.format[Summary]
  implicit val paymentObjectFormat = Json.format[PaymentObject]
}