package service.integration

import com.fincash.integration.ws.client.bsestar.upload.GetPassword
import com.fincash.integration.ws.client.bsestar.upload.MFAPI
import com.fincash.integration.ws.client.bsestar.upload.ObjectFactory
import com.google.inject.name.Named

import javax.inject.Inject
import models.integration.BSEUploadGetPassword
import models.integration.BSEUploadMfApi
import models.integration.BSEGetPasswordResponse

class BSEStarUploadPopulator @Inject()(@Named("bseUploadObjectFactory")bseUploadObjectFactory:ObjectFactory){
  
  
  def populateGetPassword(bseMfApiGetPassword:BSEUploadGetPassword):GetPassword = {
    val getPassword = new GetPassword
    
    getPassword.setUserId(bseUploadObjectFactory.createGetPasswordUserId(bseMfApiGetPassword.userId.toString))
    getPassword.setMemberId(bseUploadObjectFactory.createGetPasswordMemberId(bseMfApiGetPassword.memberId))
    getPassword.setPassword(bseUploadObjectFactory.createGetPasswordPassword(bseMfApiGetPassword.password))
    getPassword.setPassKey(bseUploadObjectFactory.createGetPasswordPassKey(bseMfApiGetPassword.passKey))
    
    getPassword
  }
  
  def populateGetMfApi(bseMfApiModel:BSEUploadMfApi,encryptedPassword:String):MFAPI = {
    val mfApi = new MFAPI
    
    mfApi.setFlag(bseUploadObjectFactory.createMFAPIFlag(bseMfApiModel.flag))
    mfApi.setUserId(bseUploadObjectFactory.createMFAPIUserId(bseMfApiModel.userId.toString))
    mfApi.setEncryptedPassword(bseUploadObjectFactory.createMFAPIEncryptedPassword(encryptedPassword))
    mfApi.setParam(bseUploadObjectFactory.createMFAPIParam(bseMfApiModel.param))
    
    mfApi
  }
}

