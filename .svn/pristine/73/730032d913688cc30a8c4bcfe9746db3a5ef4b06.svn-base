package service.integration

import models.integration.ClientFatcaUpload

import scala.collection.mutable.ListBuffer

/**
  * Created by fincash on 21-02-2017.
  */
class BSEFATCAUploadHelper {


  def getFatcaUploadValuesList(clientFatcaUpload: ClientFatcaUpload) = {

    val fatcaValuesList:ListBuffer[String] = ListBuffer[String]()

    fatcaValuesList.+=(clientFatcaUpload.pan)
    fatcaValuesList.+=(clientFatcaUpload.panExemptKYCRefNo.getOrElse(""))
    fatcaValuesList.+=(clientFatcaUpload.name)
    fatcaValuesList.+=(clientFatcaUpload.dateOfBirth.getOrElse(""))
    fatcaValuesList.+=(clientFatcaUpload.fatherName.getOrElse(""))
    fatcaValuesList.+=(clientFatcaUpload.spouseName.getOrElse(""))
    fatcaValuesList.+=(clientFatcaUpload.taxStatus)
    fatcaValuesList.+=(clientFatcaUpload.dataSrc)
    fatcaValuesList.+=(clientFatcaUpload.addressType)
    fatcaValuesList.+=(clientFatcaUpload.birthPlace)
    fatcaValuesList.+=(clientFatcaUpload.birthCountry)
    fatcaValuesList.+=(clientFatcaUpload.residenceCountry1)
    fatcaValuesList.+=(clientFatcaUpload.pin1)
    fatcaValuesList.+=(clientFatcaUpload.idType1)
    fatcaValuesList.+=(clientFatcaUpload.residenceCountry2.getOrElse(""))
    fatcaValuesList.+=(clientFatcaUpload.pin2.getOrElse(""))
    fatcaValuesList.+=(clientFatcaUpload.idType2.getOrElse(""))
    fatcaValuesList.+=(clientFatcaUpload.residenceCountry3.getOrElse(""))
    fatcaValuesList.+=(clientFatcaUpload.pin3.getOrElse(""))
    fatcaValuesList.+=(clientFatcaUpload.idType3.getOrElse(""))
    fatcaValuesList.+=(clientFatcaUpload.residenceCountry4.getOrElse(""))
    fatcaValuesList.+=(clientFatcaUpload.pin4.getOrElse(""))
    fatcaValuesList.+=(clientFatcaUpload.idType4.getOrElse(""))

    val clientIncomeDetails = clientFatcaUpload.clientIncomeDetails
    fatcaValuesList.+=(clientIncomeDetails.wealthSource)
    fatcaValuesList.+=(clientIncomeDetails.corporateServices.getOrElse(""))
    fatcaValuesList.+=(clientIncomeDetails.incomeSlab)
    fatcaValuesList.+=(clientIncomeDetails.netWorth.getOrElse(""))
    fatcaValuesList.+=(clientIncomeDetails.netWorthDate.getOrElse(""))
    fatcaValuesList.+=(clientIncomeDetails.politicallyExposedPerson)
    fatcaValuesList.+=(clientIncomeDetails.occupationCode)
    fatcaValuesList.+=(clientIncomeDetails.occupationType)
    fatcaValuesList.+=(clientIncomeDetails.exemptionCode.getOrElse(""))
    fatcaValuesList.+=(clientIncomeDetails.ffiDrnfe.getOrElse(""))
    fatcaValuesList.+=(clientIncomeDetails.giinNumber.getOrElse(""))
    fatcaValuesList.+=(clientIncomeDetails.sponsoringEntity.getOrElse(""))
    fatcaValuesList.+=(clientIncomeDetails.giinNa.getOrElse(""))
    fatcaValuesList.+=(clientIncomeDetails.giinExemptionCode.getOrElse(""))
    fatcaValuesList.+=(clientIncomeDetails.nonFinEntityCategory.getOrElse(""))
    fatcaValuesList.+=(clientIncomeDetails.nonFinEntitySubCategory.getOrElse(""))
    fatcaValuesList.+=(clientIncomeDetails.businessNature.getOrElse(""))
    fatcaValuesList.+=(clientIncomeDetails.relatedToListCompany.getOrElse(""))
    fatcaValuesList.+=(clientIncomeDetails.exchangeName)

    val uboDetails = clientIncomeDetails.clientUBODetails
    fatcaValuesList.+=(uboDetails.uboApplicable)
    fatcaValuesList.+=(uboDetails.uboCount.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboName.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboPan.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboNation.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboAdd1.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboAdd2.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboAdd3.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboCity.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboPin.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboState.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboCountry.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboAddressType.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboTaxResidenCountry.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboTIN.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboIdentificationDocType.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboBirthCountry.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboDateOfBirth.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboGender.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboFatherName.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboOccupation.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboOccupationType.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboTelNo.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboMobileNo.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboCode)
    fatcaValuesList.+=(uboDetails.uboHolPc.getOrElse(""))
    fatcaValuesList.+=(uboDetails.selfDeclaration.getOrElse(""))
    fatcaValuesList.+=(uboDetails.uboDeclarationFlag)
    fatcaValuesList.+=(uboDetails.reportingPersonAdhaar.getOrElse(""))
    fatcaValuesList.+=(uboDetails.newOrUpdate)
    fatcaValuesList.+=(uboDetails.logName)
    fatcaValuesList.+=(uboDetails.filler1.getOrElse(""))
    fatcaValuesList.+=(uboDetails.filler2.getOrElse(""))

    fatcaValuesList
  }
}
