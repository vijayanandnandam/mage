package helpers.integration.bse

import com.fincash.integration.ws.client.bsestar.upload.{GetPassword, MFAPI}

import scala.collection.mutable.ListBuffer

/**
  * Created by fincash on 23-01-2017.
  */
object AdditionalRequestLogHelper {

  def getPasswordParameters(getPassword: GetPassword):(ListBuffer[String],ListBuffer[String]) = {

    val paramNameList:ListBuffer[String] = ListBuffer[String]()
    val paramValueList:ListBuffer[String] = ListBuffer[String]()

    paramNameList.+=(getPassword.getUserId.getName.getLocalPart)
    paramValueList.+=(getPassword.getUserId.getValue)

    paramNameList.+=(getPassword.getPassword.getName.getLocalPart)
    paramValueList.+=(getPassword.getPassword.getValue)

    paramNameList.+=(getPassword.getPassKey.getName.getLocalPart)
    paramValueList.+=(getPassword.getPassKey.getValue)

    (paramNameList,paramValueList)

  }

  def getMfApiParameters(mFAPI: MFAPI):(ListBuffer[String],ListBuffer[String]) = {

    val paramNameList:ListBuffer[String] = ListBuffer[String]()
    val paramValueList:ListBuffer[String] = ListBuffer[String]()

    paramNameList.+=(mFAPI.getFlag.getName.getLocalPart)
    paramValueList.+=(mFAPI.getFlag.getValue)

    paramNameList.+=(mFAPI.getUserId.getName.getLocalPart)
    paramValueList.+=(mFAPI.getUserId.getValue)

    paramNameList.+=(mFAPI.getEncryptedPassword.getName.getLocalPart)
    paramValueList.+=(mFAPI.getEncryptedPassword.getValue)

    paramNameList.+=(mFAPI.getParam.getName.getLocalPart)
    paramValueList.+=(mFAPI.getParam.getValue)

    (paramNameList,paramValueList)
  }
}
