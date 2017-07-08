package helpers.integration.bse

import com.fincash.integration.ws.client.bsestar.upload.{GetPasswordResponse, MFAPIResponse}

import scala.collection.mutable.ListBuffer

/**
  * Created by fincash on 24-01-2017.
  */
object AdditionalResponseLogHelper {

  def getPasswordParameters(getPasswordResponse: GetPasswordResponse):(ListBuffer[String],ListBuffer[String]) = {

    val paramNameList:ListBuffer[String] = ListBuffer[String]()
    val paramValueList:ListBuffer[String] = ListBuffer[String]()

    paramNameList.+=(getPasswordResponse.getGetPasswordResult.getName.getLocalPart)
    paramValueList.+=(getPasswordResponse.getGetPasswordResult.getValue)

    (paramNameList,paramValueList)

  }
  def getMfApiParameters(mFAPIResponse: MFAPIResponse):(ListBuffer[String],ListBuffer[String]) = {

    val paramNameList:ListBuffer[String] = ListBuffer[String]()
    val paramValueList:ListBuffer[String] = ListBuffer[String]()

    paramNameList.+=(mFAPIResponse.getMFAPIResult.getName.getLocalPart)
    paramValueList.+=(mFAPIResponse.getMFAPIResult.getValue)

    (paramNameList,paramValueList)
  }
}
