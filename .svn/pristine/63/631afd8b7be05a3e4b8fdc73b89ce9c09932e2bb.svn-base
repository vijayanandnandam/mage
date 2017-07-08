package service.integration

import java.util.Date
import javax.inject.Inject

import constants.{BaseConstants, DBConstants, DateConstants, OrderConstants}
import models._
import models.integration._
import org.slf4j.LoggerFactory
import service.CNDService
import utils.DateTimeUtils
import utils.bse.BSEUtility

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 21-02-2017.
  */
class BSEClientHelper @Inject()(implicit ec: ExecutionContext, cNDService: CNDService) extends DBConstants
  with OrderConstants with BaseConstants with DateConstants {

  val logger, log = LoggerFactory.getLogger(classOf[BSEClientHelper])


  def getClientRegisterValuesList(bSEClientDetailsModel: BSEClientDetailsModel): ListBuffer[String] = {

    val clientRegValues: ListBuffer[String] = ListBuffer[String]()
    clientRegValues.+=(bSEClientDetailsModel.clientCode)
    clientRegValues.+=(bSEClientDetailsModel.clientType)
    clientRegValues.+=(bSEClientDetailsModel.taxStatus)
    clientRegValues.+=(bSEClientDetailsModel.occupationCode)
    clientRegValues.+=(bSEClientDetailsModel.applicantName1)
    clientRegValues.+=(bSEClientDetailsModel.applicantName2.getOrElse(""))
    clientRegValues.+=(bSEClientDetailsModel.applicantName3.getOrElse(""))
    clientRegValues.+=(bSEClientDetailsModel.dateOfBirth)
    clientRegValues.+=(bSEClientDetailsModel.gender)
    clientRegValues.+=(bSEClientDetailsModel.guardianFather.getOrElse(""))
    clientRegValues.+=(bSEClientDetailsModel.pan.getOrElse(""))
    clientRegValues.+=(bSEClientDetailsModel.nominee.getOrElse(""))
    clientRegValues.+=(bSEClientDetailsModel.nomineeRelation.getOrElse(""))
    clientRegValues.+=(bSEClientDetailsModel.guardianPan.getOrElse(""))
    clientRegValues.+=(bSEClientDetailsModel.physicalDematType)
    clientRegValues.+=(bSEClientDetailsModel.defaultDp.getOrElse(""))
    clientRegValues.+=(bSEClientDetailsModel.cdslPid.getOrElse(""))
    clientRegValues.+=(bSEClientDetailsModel.cdslCltId.getOrElse(""))
    clientRegValues.+=(bSEClientDetailsModel.ndslPid.getOrElse(""))
    clientRegValues.+=(bSEClientDetailsModel.ndslCltId.getOrElse(""))

    val accountDetails = bSEClientDetailsModel.accountDetails
    clientRegValues.+=(accountDetails.accType1)
    clientRegValues.+=(accountDetails.accNo1)
    clientRegValues.+=(accountDetails.micrNo1)
    clientRegValues.+=(accountDetails.neftOrIfscCode1)
    clientRegValues.+=(accountDetails.defaultBankFlag1)
    clientRegValues.+=(accountDetails.accType2.getOrElse(""))
    clientRegValues.+=(accountDetails.accNo2.getOrElse(""))
    clientRegValues.+=(accountDetails.micrNo2.getOrElse(""))
    clientRegValues.+=(accountDetails.neftOrIfscCode2.getOrElse(""))
    clientRegValues.+=(accountDetails.defaultBankFlag2.getOrElse(""))
    clientRegValues.+=(accountDetails.accType3.getOrElse(""))
    clientRegValues.+=(accountDetails.accNo3.getOrElse(""))
    clientRegValues.+=(accountDetails.micrNo3.getOrElse(""))
    clientRegValues.+=(accountDetails.neftOrIfscCode3.getOrElse(""))
    clientRegValues.+=(accountDetails.defaultBankFlag3.getOrElse(""))
    clientRegValues.+=(accountDetails.accType4.getOrElse(""))
    clientRegValues.+=(accountDetails.accNo4.getOrElse(""))
    clientRegValues.+=(accountDetails.micrNo4.getOrElse(""))
    clientRegValues.+=(accountDetails.neftOrIfscCode4.getOrElse(""))
    clientRegValues.+=(accountDetails.defaultBankFlag4.getOrElse(""))
    clientRegValues.+=(accountDetails.accType5.getOrElse(""))
    clientRegValues.+=(accountDetails.accNo5.getOrElse(""))
    clientRegValues.+=(accountDetails.micrNo5.getOrElse(""))
    clientRegValues.+=(accountDetails.neftOrIfscCode5.getOrElse(""))
    clientRegValues.+=(accountDetails.defaultBankFlag5.getOrElse(""))
    clientRegValues.+=(accountDetails.chequeName.getOrElse(""))

    val addressDetails = bSEClientDetailsModel.addressDetails
    clientRegValues.+=(addressDetails.add1)
    clientRegValues.+=(addressDetails.add2.getOrElse(""))
    clientRegValues.+=(addressDetails.add3.getOrElse(""))
    clientRegValues.+=(addressDetails.city)
    clientRegValues.+=(addressDetails.state)
    clientRegValues.+=(addressDetails.pincode)
    clientRegValues.+=(addressDetails.country)
    clientRegValues.+=(addressDetails.resiphone.getOrElse(""))
    clientRegValues.+=(addressDetails.resiFax.getOrElse(""))
    clientRegValues.+=(addressDetails.officePhone.getOrElse(""))
    clientRegValues.+=(addressDetails.officeFax.getOrElse(""))
    clientRegValues.+=(addressDetails.email)
    clientRegValues.+=(addressDetails.commode)
    clientRegValues.+=(addressDetails.divPayMode)
    clientRegValues.+=(addressDetails.pan2.getOrElse(""))
    clientRegValues.+=(addressDetails.pan3.getOrElse(""))
    clientRegValues.+=(addressDetails.mapinNo.getOrElse(""))

    val foreignAddDetails = addressDetails.foreignAddressDetails
    clientRegValues.+=(foreignAddDetails.add1.getOrElse(""))
    clientRegValues.+=(foreignAddDetails.add2.getOrElse(""))
    clientRegValues.+=(foreignAddDetails.add3.getOrElse(""))
    clientRegValues.+=(foreignAddDetails.city.getOrElse(""))
    clientRegValues.+=(foreignAddDetails.state.getOrElse(""))
    clientRegValues.+=(foreignAddDetails.pincode.getOrElse(""))
    clientRegValues.+=(foreignAddDetails.country.getOrElse(""))
    clientRegValues.+=(foreignAddDetails.resiphone.getOrElse(""))
    clientRegValues.+=(foreignAddDetails.resiFax.getOrElse(""))
    clientRegValues.+=(foreignAddDetails.officePhone.getOrElse(""))
    clientRegValues.+=(foreignAddDetails.officeFax.getOrElse(""))
    clientRegValues.+=(addressDetails.mobile)

    clientRegValues
  }

  def populateClientModel(clientCode: Long, userBasic: UserBasic, userAddress: UserAddress, userBank: UserBank, userFatca: UserFatca,nominee: Option[Nominee]): Future[Option[BSEClientDetailsModel]] = {

    val contactDetails = userBasic.contact.get
    val addressDetails = userAddress.permanentAddress.get
    val bankDetails = userBank.bank.get

    if(isClientAddressDetailsValid(addressDetails) && !userFatca.occupation.isEmpty && !userBank.accountType.isEmpty && !userBank.accountNumber.isEmpty && !userBasic.panName.isEmpty){

      for {
        stateCode <- cNDService.getBSECodeByCndPk(addressDetails.state.get.toLong)
        taxStatusCode <- cNDService.getBSECodeByCndName(INDIVIDUAL)
        countryName <- cNDService.getCndNameByCndPk(addressDetails.country.get.toLong)
        occupationCode <- cNDService.getBSEExtFld2ByCndPk(userFatca.occupation.get.toLong)
        accountTypeCode <- cNDService.getBSECodeByCndPk(userBank.accountType.get.toLong)
        nomineeRelation <- if(nominee.isEmpty || nominee.get.nomineeRelation.isEmpty || nominee.get.nomineeRelation.get.length == 0) {
          Future{None}
        } else {
          cNDService.getCndNameByCndPk(nominee.get.nomineeRelation.get.toLong)
        }
      } yield {

        var clientName = userBasic.panName.get
        val clientGender = BSEUtility.getGender(userBasic.gender.get)
        logger.debug(userBasic.dob.get.toString)
        val clientDob = DateTimeUtils.convertStringYYYYMMDDDateToDDMMYYYY(userBasic.dob.get)
        logger.debug(clientDob.toString)
        val bseAddressValues = BSEUtility.convertToBSEAddress(addressDetails.address1.get, addressDetails.address2.get)
        val addressField1 = bseAddressValues._1
        val addressField2 = bseAddressValues._2
        val addressField3 = bseAddressValues._3
        val nomineeName = if(nominee.isEmpty)None else nominee.get.nomineeName
        val clientAddressDetails: ClientAddressDetails = ClientAddressDetails(addressField1, Some(addressField2), Some(addressField3),
          addressDetails.city.get, stateCode.get, addressDetails.pin.get, countryName.get, contactDetails.mob.get,
          contactDetails.tel, contactDetails.fax, None, None, contactDetails.email.get, COMMUNICATION_MODE_EMAIL, DIRECT_CREDIT, ClientForeignAddDetails())

        val clientAccountDetails: ClientAccountDetails = ClientAccountDetails(accountTypeCode.get, userBank.accountNumber.get,
          bankDetails.MICR.get, bankDetails.IFSC.get, YES)

        val bSEClientDetailsModel = BSEClientDetailsModel(clientCode.toString, SINGLE, taxStatusCode.get, occupationCode.get, clientName, None, None,
          clientDob, clientGender, PHYSICAL_MODE, clientAccountDetails, clientAddressDetails, None, userBasic.pan,nomineeName,nomineeRelation)
        Some(bSEClientDetailsModel)
      }
    } else{
      Future{
        None
      }
    }
  }

  def isClientAddressDetailsValid(addressDetails: Address):Boolean = {

    if (addressDetails.state.isEmpty || addressDetails.country.isEmpty ||
      addressDetails.pin.isEmpty || addressDetails.country.isEmpty ||
      addressDetails.city.isEmpty || addressDetails.address1.isEmpty ||
      addressDetails.address2.isEmpty) {
      logger.debug("User Address Details Not valid: "+ addressDetails)
      false
    } else {
      true
    }
  }

  def populateClientFatcaModel(ipAddress: String, isUpdate: Boolean, userBasic: UserBasic, userAddress: UserAddress, userFatca: UserFatca): Future[Option[ClientFatcaUpload]] = {

    var newOrChangeValue = NEW_FATCA
    if (isUpdate) {
      newOrChangeValue = CHANGE_FATCA
    }
    val logName = ipAddress + "#" + DateTimeUtils.convertDateToFormat(new Date(), BSE_LOG_NAME_FORMAT)
    val clientUboDetails = ClientUBODetails(NO, UBO_CODE_UNKNOWN, NO, newOrChangeValue, logName)
    val addressDetails = userAddress.permanentAddress.get
    if(isClientFatcaValid(userFatca) && !addressDetails.country.isEmpty && !addressDetails.addressType.isEmpty && isUserBasicDetailsValid(userBasic)){

      for {
        incomeSlabCode <- cNDService.getBSECodeByCndPk(userFatca.income.get.toLong)
        occupationCode <- cNDService.getBSECodeByCndPk(userFatca.occupation.get.toLong)
        taxStatusCode <- cNDService.getBSECodeByCndName(INDIVIDUAL)
        addressType <- cNDService.getBSECodeByCndPk(addressDetails.addressType.get.toLong)
        birthCountryName <- cNDService.getBSECodeByCndPk(userFatca.birthCountry.get.toLong)
        residenceCountry <- cNDService.getBSECodeByCndPk(addressDetails.country.get.toLong)
        occupationType <- cNDService.getCndExtField1ByCndPk(userFatca.occupation.get.toLong)
        sourceOfWealth <- cNDService.getBSECodeByCndPk(userFatca.sourceOfWealth.get.toLong)
      } yield {
        var clientName = userBasic.panName.get

        val clientIncomeDetails = ClientIncomeDetails(sourceOfWealth.get, BSE_EXCHANGE_NAME, incomeSlabCode.get, userFatca.politicallyExposed.get,
          occupationCode.get, occupationType.get, clientUboDetails)

        val clientFatcaUpload = ClientFatcaUpload(userBasic.pan.get, clientName, taxStatusCode.get, FATCA_ELECTRONIC_DATA_SOURCE, addressType.get, userFatca.birthCity.get,
          birthCountryName.get, residenceCountry.get, userBasic.pan.get, FATCA_PAN_IDENTIFICATION_TYPE_CODE, clientIncomeDetails)

        Some(clientFatcaUpload)
      }
    } else{
      Future{
        None
      }
    }
  }

  def isClientFatcaValid(userFatca: UserFatca):Boolean = {

    if(userFatca.income.isEmpty || userFatca.occupation.isEmpty ||
      userFatca.birthCountry.isEmpty || userFatca.sourceOfWealth.isEmpty ||
      userFatca.politicallyExposed.isEmpty || userFatca.birthCity.isEmpty){

      logger.debug("Fatca Not valid: "+ userFatca)
      false
    } else{
      true
    }
  }

  def isUserBasicDetailsValid(userBasic: UserBasic):Boolean = {

    if(userBasic.panName.isEmpty || userBasic.pan.isEmpty){
      logger.debug("User Basic Details Not valid: "+ userBasic)
      false
    } else{
      true
    }
  }
}
