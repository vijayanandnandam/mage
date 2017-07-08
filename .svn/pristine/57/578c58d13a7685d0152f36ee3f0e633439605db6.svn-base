package service

import javax.inject.Inject

import helpers.AMCHelper
import repository.module.ApplicationConstantRepository

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 06-04-2017.
  */
class AMCService @Inject()(implicit ec: ExecutionContext, appConsRepository: ApplicationConstantRepository, aMCHelper: AMCHelper) {

  def isISIPAllowed(amctrfnum: Long): Future[Boolean] = {

    appConsRepository.getIsipAmcs().map(actRowList => {
      if (actRowList.isEmpty) {
        false
      } else {
        val amctRfnumString = actRowList.head.actconstantvalue
        val isipAmcList = amctRfnumString.split(',')
        aMCHelper.isISIPAllowed(amctrfnum, isipAmcList)
      }
    })

  }
}
