/**
  * Created by Fincash on 24-01-2017.
  */

package controllers

import java.io.File
import javax.inject.Inject

import constants.{CNDConstants, DBConstants, DateConstants, KycConstants}
import helpers.{AuthenticatedAction, MailHelper, UserHelper}
import models.UserJsonFormats._
import models._
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import repository.module.{KycRepository, UserRepository}
import repository.tables.{FceubdRepo, FckycRepo, FcubdRepo}
import service.{MailService, PropertiesLoaderService, SolrCNDSearchService, UserService}
import utils.{DateTimeUtils, RequestUtils}

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


class UserController @Inject()(implicit val ec: ExecutionContext, auth: AuthenticatedAction, userService: UserService,
                               application: Application, userRepository: UserRepository, userHelper: UserHelper,
                               solrCNDSearchService: SolrCNDSearchService, kycRepository: KycRepository,
                               fcubdRepo: FcubdRepo, fceubdRepo: FceubdRepo, fckycRepo:FckycRepo, mailHelper: MailHelper, mailService: MailService)
  extends Controller with CNDConstants with KycConstants with DBConstants with DateConstants {

  val logger, log = LoggerFactory.getLogger(classOf[UserController])


  def getUserData = auth.Action { request =>
    val requestData = request.body
    var token = request.token;

    //    var user = userService.getUserDataFromDb(request);
    Ok;
  }

  def getFirstName = auth.Action.async { request => {
    userService.getUsernameFromRequest(request).flatMap(username => {
      userRepository.getFirstNameByUsername(username.get).map(firstName => {
        Ok(Json.obj("firstname" -> firstName))
      })
    })
  }
  }

  def getUserDetails = auth.Action.async { request =>
    userService.getUseridFromRequest(request).flatMap { userid => {
      logger.debug("user  id >>>    " + userid)
      if (userid.nonEmpty) {
        userRepository.getUserByPk(userid.get).map { user => {
          Ok(Json.obj("mob" -> user.get.ubdmobileno, "email" -> user.get.ubdemailid, "mobRegDate" -> user.get.modifydate, "pan" -> user.get.ubdpan))
        }
        }
      } else {
        Future.apply(Ok(Json.obj("mob" -> "", "email" -> "", "mobRegDate" -> "", "pan" -> "")))
      }
    }
    }
  }

  def panCheck = auth.Action.async(parse.json) { request => {
    var requestData = request.body
    var pan = (requestData \ "pan").as[String].toUpperCase
    userService.getUsernameFromRequest(request).flatMap { username => {
      userRepository.getUserIdByUsername(username.get).flatMap { userid => {
        userRepository.checkPanExists(userid, pan).map(panExists => {
          if (panExists) {
            Ok(Json.obj("sucess" -> false, "message" -> "PAN already exists"))
          }
          else {
            Ok(Json.obj("success" -> true, "message" -> "PAN doesn't exists"))
          }
        })
      }
      }
    }
    }
  }
  }

  def panUpdate = auth.Action.async(parse.json) { request => {
    var requestData = request.body
    var pan = (requestData \ "pan").as[String].toUpperCase

    userService.getUsernameFromRequest(request).flatMap { username => {
      logger.debug("Username " + username)
      userRepository.getUserIdByUsername(username.get).flatMap { userid => {
        userRepository.checkPanExists(userid, pan).flatMap(panExists => {
          if (!panExists) {
            userRepository.savePanNumber(userid, pan).map(data => {
              if (data) {
                Ok(Json.obj("success" -> true, "message" -> "PAN Updated successfully"))
              }
              else {
                Ok(Json.obj("success" -> false, "message" -> "PAN not saved"))
              }
            })
          }
          else {
            Future.apply(Ok(Json.obj("success" -> false, "message" -> "PAN already exists")))
          }
        })
      }
      }
    }
    }
  }
  }

  def mobUpdate = auth.Action.async(parse.json) { request => {
    var requestData = request.body

    var mob = (requestData \ "mob").as[String]

    userService.getUsernameFromRequest(request).flatMap { username => {
      logger.debug("Username " + username)
      userRepository.getUserIdByUsername(username.get).map { userid => {
        userRepository.saveMobNumber(userid, mob)
        Ok(Json.obj("success" -> true, "message" -> "Mobile number saved successfully"))
      }
      }
    }
    }
  }
  }

  def postUserData = auth.Action.async(parse.json) { request =>
    var requestData = request.body
    logger.debug(request.body.toString)

    var userBasic = (requestData \ "userBasic").as[UserBasic]
    //    var userBank = (requestData \ "userBank").as[UserBank]
    var userAddress = (requestData \ "userAddress").as[UserAddress]
    /*var userFatca = (requestData \ "userFatca").as[UserFatca]*/

    userService.getUsernameFromRequest(request).flatMap { username => {
      logger.debug("Username " + username)
      userRepository.getUserIdByUsername(username.get).map { userid => {
        var ubdrfnum = userRepository.saveUserBasic(userid, username.get, userBasic)
        var buarfnum = userRepository.saveUserAddresses(userid, username.get, userAddress, userBasic)
        Ok
      }
      }
    }
    }
  }

  /** ****************GET DATA ***************************/

  def getUserBasicData = auth.Action.async { request => {
    userService.getUsernameFromRequest(request).flatMap(username => {
      userRepository.getUserIdByUsername(username.get).flatMap(userid => {
        userRepository.getUserBasic(userid).map(userBasic => {
          Ok(userHelper.getUserBasicObject(userBasic))
        })
      })
    })
  }
  }

  def getUserFatcaData = auth.Action.async { request => {
    userService.getUsernameFromRequest(request).flatMap(username => {
      userRepository.getUserIdByUsername(username.get).flatMap(userid => {
        userRepository.getUserFatca(userid).map(userFatca => {
          Ok(userHelper.getUserFactaObject(userFatca))
        })
      })
    })
  }
  }

  def getUserBankData = auth.Action.async { request => {
    userService.getUsernameFromRequest(request).flatMap(username => {
      userRepository.getUserIdByUsername(username.get).flatMap(userid => {
        userRepository.getUserBank(userid).map(userBank => {
          Ok(userHelper.getUserBankObject(userBank))
        })
      })
    })
  }
  }

  def getUserNomineeData = auth.Action.async(request => {
    userService.getUsernameFromRequest(request).flatMap(username => {
      userRepository.getUserIdByUsername(username.get).flatMap(userid => {
        userRepository.getUserNominee(userid).map(userNominee => {
          Ok(userHelper.getUserNomineeObject(userNominee))
        })
      })
    })
  })

  def getUserAddressData = auth.Action.async(request => {
    userService.getUsernameFromRequest(request).flatMap(username => {
      userRepository.getUserIdByUsername(username.get).flatMap(userid => {
        userRepository.getUserAddress(userid).map(userAddress => {
          Ok(userHelper.getUserAddressObject(userAddress))
        })
      })
    })
  })

  /** ****************POST DATA **************************/

  def postUserBasicData = auth.Action.async(parse.json) { request => {
    val requestData = request.body
    logger.debug(request.body.toString)
    var pan = ""
    val userBasic = requestData.as[UserBasic]
    if (userBasic.pan.nonEmpty) {
      pan = userBasic.pan.get.toUpperCase
    }
    userService.getUsernameFromRequest(request).flatMap { username => {
      logger.debug("Username " + username)
      userRepository.getUserIdByUsername(username.get).flatMap { userid => {
        userRepository.checkPanExists(userid, pan).flatMap { panExists => {
          if (!panExists) {
            userRepository.saveUserBasic(userid, username.get, userBasic).map(data => {
              if (data) {
                Ok(Json.obj("success" -> true, "message" -> "Basic details Saved successfully"))
              }
              else {
                Ok(Json.obj("success" -> false, "message" -> "Basic details not saved"))
              }
            })
          }
          else {
            Future.apply(Ok(Json.obj("success" -> false, "message" -> "Pan already exists")))
          }
        }
        }

      }
      }
    }
    }
  }
  }

  def postUserAddressData = auth.Action.async(parse.json) { request => {
    var requestData = request.body

    var userBasic = (requestData \ "userBasic").as[UserBasic]
    var userAddress = (requestData \ "userAddress").as[UserAddress]

    userService.getUsernameFromRequest(request).flatMap { username => {
      logger.debug("Username " + username)
      userRepository.getUserIdByUsername(username.get).map { userid => {
        var res = userRepository.saveUserAddresses(userid, username.get, userAddress, userBasic)
        Ok(Json.obj("amtP" -> res._1, "amtC" -> res._2))
      }
      }
    }
    }
  }
  }

  def postUserFatcaData = auth.Action.async(parse.json) { request => {
    var requestData = request.body
    var userFatca = requestData.as[UserFatca]
    userService.getUsernameFromRequest(request).flatMap { username => {
      logger.debug("Username " + username)
      userRepository.getUserIdByUsername(username.get).flatMap { userid => {
        userRepository.saveUserFatca(userid, username.get, userFatca).map(futid => {
          Ok(Json.obj("futid" -> futid))
        }).recover({
          case ex: Exception =>
            logger.error(ex.getMessage + " Can't Update User Fatca")
            throw ex
        })
      }
      }
    }
    }
  }
  }

  def postUserBankData = auth.Action.async(parse.json) { request => {
    var requestData = request.body
    var userBank = requestData.as[UserBank]
    userService.getUsernameFromRequest(request).flatMap { username => {
      logger.debug("Username " + username)
      userRepository.getUserIdByUsername(username.get).flatMap { userid => {
        userRepository.saveUserBanks(userid, username.get, userBank).map(buaid => {
          Ok(Json.obj("buaid" -> buaid))
        })
      }
      }
    }
    }
  }
  }

  def postUserNomineeData = auth.Action.async(parse.json) { request => {
    var requestData = request.body
    var userNominee = requestData.as[Nominee]
    userService.getUsernameFromRequest(request).flatMap { username => {
      logger.debug("Username " + username)
      userRepository.getUserIdByUsername(username.get).flatMap { userid => {
        userRepository.saveUserNominee(userid, username.get, userNominee).map(ndtid => {
          Ok(Json.obj("ndtid" -> ndtid))
        })
      }
      }
    }
    }
  }
  }

  /** ****************POST EKYC data **********************/
  def postUserEkycData = Action.async(parse.json) { request => {
    val requestData = request.body
    var userBasic = (requestData \ "userBasic").as[UserBasic]
    var userAddress = (requestData \ "userAddress").as[UserAddress]
    var eKycApiData = (requestData \ "eKycApiData").as[EKycApiData]
    var userid = eKycApiData.userid.get.toLong

    /** **************SET STATE & ADDRESS TYPE ****************************************/
    var aadharState = eKycApiData.aadharState.getOrElse("")
    var aadharAddressType = eKycApiData.aadharAddressType.getOrElse("")
    var staterfnum, addresstyperfnum: Option[String] = None
    if (aadharState.length > 0) {
      val results: Option[CNDDoc] = solrCNDSearchService.cndSearch(STATE, aadharState).headOption
      logger.debug("AAdhar State : " + results);
      if (!results.isEmpty)
        staterfnum = Some(results.get.cndRfnum.toString)
    }
    if (aadharAddressType.length > 0) {
      val results: Option[CNDDoc] = solrCNDSearchService.cndSearch(ADDRESS_TYPE, aadharAddressType).headOption
      logger.debug("Address Type : " + results);
      if (!results.isEmpty)
        addresstyperfnum = Some(results.get.cndRfnum.toString)
    }

    val address = userAddress.permanentAddress.get.copy(state = staterfnum, addressType = addresstyperfnum)
    val newAddress = UserAddress(Option(address), Option(address), true)

    /** ***********************SET OCCUPATION & NATIONALITY ***************************/
    var aadharOccupation = eKycApiData.aadharOccupation.getOrElse("")
    var occupationrfnum: Option[String] = None
    if (aadharOccupation.length > 0) {
      val results: Option[CNDDoc] = solrCNDSearchService.cndSearch(OCCUPATION, aadharOccupation).headOption
      logger.debug("Occupation : " + results);
      if (!results.isEmpty)
        occupationrfnum = Some(results.get.cndRfnum.toString)
    }

    var aadharNationality = eKycApiData.aadharNationality.getOrElse("India")
    var nationalityrfnum: Option[String] = None
    if (aadharNationality.length > 0) {
      val results: Option[CNDDoc] = solrCNDSearchService.cndSearch(COUNTRY, aadharNationality).headOption
      if (!results.isEmpty)
        nationalityrfnum = Some(results.get.cndRfnum.toString)
    }

    val userFatca = new UserFatca(None, None, occupationrfnum, None, nationalityrfnum, None, None, None, None, None, None, None)

    /** ***************************SET GENDER & MARITAL STATUS **************************/
    var aadharMarital = userBasic.maritalStatus.getOrElse("")
    var maritalrfnum: Option[String] = None
    if (aadharMarital.length > 0) {
      val results: Option[CNDDoc] = solrCNDSearchService.cndSearch(MARITAL_STATUS, aadharMarital).headOption
      logger.debug("Marital Status : " + results);
      if (!results.isEmpty)
        maritalrfnum = Some(results.get.cndRfnum.toString)
    }

    if (userBasic.gender.getOrElse("").equalsIgnoreCase("male")) {
      userBasic = userBasic.copy(gender = Some(MALE), maritalStatus = maritalrfnum)
    }
    else if (userBasic.gender.getOrElse("").equalsIgnoreCase("female")) {
      userBasic = userBasic.copy(gender = Some(FEMALE), maritalStatus = maritalrfnum)
    } else {
      userBasic = userBasic.copy(gender = Some(OTHER), maritalStatus = maritalrfnum)
    }
    if (userBasic.dob.nonEmpty)
      userBasic = userBasic.copy(dob = DateTimeUtils.convertStringDateWithFormats(userBasic.dob.get, AADHAAR_DATE_FORMAT, YYYYMMDD))

    /** ****************************DB ACTIONS ****************************************************/
    userRepository.getUsernameByUserid(userid).flatMap(username => {
      userRepository.saveUserAddresses(userid, username, newAddress, userBasic)
      userRepository.saveUserBasic(userid, username, userBasic).flatMap(status => {
        if (status) {
          userRepository.saveUserFatca(userid, username, userFatca).flatMap(_futid => {
            if (_futid.nonEmpty) {
              userRepository.saveUserAadhar(userid, username, eKycApiData).flatMap(_status => {
                if (_status) {
                  kycRepository.updateuserKYCStatus(userid, username, KYC_EXTERNALLY_DONE, KYCTYPE_AADHAR).map(_ => {

                    val mailResponse = userService.sendeKYCMail(userid, true).map(value => {
                      logger.debug(value.toString)
                    })
                    Ok(Json.obj("success" -> true))
                  })
                } else {
                  Future.apply(Ok(Json.obj("success" -> false)))
                }
              })
            } else {
              Future.apply(Ok(Json.obj("success" -> false)))
            }
          })
        } else {
          Future.apply(Ok(Json.obj("success" -> false)))
        }
      })
    })
  }
  }

  /** *******************ACTIVATE USER ***************************************/
  def activateUser = Action.async(parse.json) { request => {
    val ipAddress = RequestUtils.getIpAddress(request)
    val requestData = request.body
    logger.debug(requestData.toString())
    val userid = (requestData \ "userid").as[String].toLong
    userService.activateUser(userid, ipAddress).map{ value => {
      if (value) {
        userService.approveDocuments(userid).map(retval => {
        })
        val mailResponse = this.userService.sendRegistrationStatusMail(userid).map(value => {
          logger.debug(value.toString)
        })
        Ok(Json.obj("success" -> true))
      }
      else {
        Ok(Json.obj("success" -> false))
      }
    }}
  }}

  def registerInBSE = Action.async(parse.json) { request => {
    val ipAddress = RequestUtils.getIpAddress(request)
    val requestData = request.body
    logger.debug(requestData.toString())
    val userid = (requestData \ "userid").as[String].toLong
    userService.bseRegistration(userid, ipAddress).map(value => {
      Ok(Json.obj("bse" -> value._1, "fatca" -> value._2))
    })
  }
  }

  def isUserVarified = auth.Action.async { request => {
    userService.getUseridFromRequest(request).flatMap(userid => {
      userService.getEUBDbyPk(userid.get).map { row => {
        if (row.nonEmpty) {
          Ok(Json.obj("success" -> true, "status" -> row.get.eubdisvarified))
        }
        else {
          Ok(Json.obj("success" -> false, "status" -> "User not varified"))
        }
      }
      }
    })
  }
  }

  def sendRegistrationMail = Action.async { request => {

    val heading = PropertiesLoaderService.getConfig().getString("mail.registration.heading")
    val subj = PropertiesLoaderService.getConfig().getString("mail.registration.subject")
    val fromRegMail = this.mailHelper.fromRegMail
    val replytoRegMail = this.mailHelper.replytoRegMail
    val bccReg = this.mailHelper.bccReg
    val toRegMail = this.mailHelper.toRegMail
    var bccList: Option[ListBuffer[String]] = None
    if (bccReg != null && bccReg.trim.length > 0) {
      val bcc = ListBuffer[String]()
      bcc.+=(bccReg)
      bccList = Some(bcc)
    }
    userService.getUserObjectFromReq(request).flatMap(userLoginObject => {

      val userName: String = userLoginObject.get.username.getOrElse("")
      var userId: Long = userLoginObject.get.userid.getOrElse(0L)
      val baseUrl = this.mailHelper.baseUrl
      var kycstatus = KYC_NOTDONE


      kycRepository.getDmtIds(userId).flatMap(dmtObj => {

        val photopath = dmtObj._11
        val panpath = dmtObj._12
        val addresspath = dmtObj._13
        val bankpath = dmtObj._14
        val signaturepath = dmtObj._15

        val pathMap = Map("photograph" -> photopath, "pan" -> panpath, "address" -> addresspath, "bank" -> bankpath, "signature" -> signaturepath)

        logger.debug("phoot--->" + photopath + "pan-->" + panpath + "address-->" + addresspath + "bank-->" + bankpath + "sign--->" + signaturepath)

        fcubdRepo.getById(userId).flatMap(ubdRowOpion => {

          fceubdRepo.getById(userId).flatMap(eubdRow => {
            if (eubdRow.get.eubdisvarified == Y_FLAG) {
              Future.apply(Ok(Json.obj("Success" -> true, "message" -> "user already verified")))
            } else {
              if (ubdRowOpion.nonEmpty) {
                val ubdRow = ubdRowOpion.get
                userRepository.getUserAddress(userId).flatMap(amtRowRes => {
                  val amtRow = amtRowRes
                  userRepository.getUserBank(userId).flatMap(userBank => {
                    val bankDetails = userBank
                    userRepository.getUserFatca(userId).flatMap(userFatca => {
                      val fatcaDetails = userFatca

                      kycRepository.getUserKYCStatus(userId).map(kycRowList => {
                        if (kycRowList.isEmpty) {
                          kycstatus = "Not Available"
                        } else {
                          var kycSt = kycRowList.head.kycstatus
                          if (kycSt == KYC_DONE) {
                            kycstatus = "KYC Done"
                          }
                          else if (kycSt == KYC_UNDERPROCESS) {
                            kycstatus = "KYC under-process"
                          }
                          else if (kycSt == KYC_EXTERNALLY_DONE) {
                            kycstatus = "KYC externally done"
                          }
                          else if (kycSt == KYC_NOTDONE) {
                            kycstatus = "KYC not done"
                          }
                        }
                      val mailHeaderTemplate = views.html.mailHeader(heading, mailHelper.getMth)
                      val mailBodyTemplate = views.html.registrationVerification(ubdRow, amtRow, bankDetails, fatcaDetails, kycstatus, mailHelper.getMth)
                      val mailTemplate = views.html.mail(mailHeaderTemplate, mailBodyTemplate, mailHelper.getMth)
                      val bodyText = views.html.registrationVerificationTxt(ubdRow, amtRow, bankDetails, fatcaDetails, kycstatus, mailHelper.getMth).toString()
                      val bodyHTML = mailTemplate.toString()
                      var attachmentFiles = new mutable.HashMap[String, String]()
                      var i = 0
                      var attachments = ListBuffer[Future[File]]()
                      for ((k, v) <- pathMap) {
                        i += 1
                        val url = v
                        if (url.length > 0) {
                          if (url.indexOf('.') > (-1)) {
                            val fileType = url.split('.').last
                            val downloadurl = baseUrl + "/" + url
                            logger.debug("Download url [" + downloadurl + "]")
                            attachments.+=(mailHelper.downLoadFile(downloadurl, fileType).map(_file => {
                              attachmentFiles += (k + "_" + _file.getName -> _file.getCanonicalPath)
                              logger.debug("Downloaded Succesfully [" + _file.getName + " " + _file.getCanonicalPath + "]")
                              _file
                            }))
                          }
                        }
                      }
                      Await.result(Future.sequence(attachments), Duration(60000, "millis")) /*.map(_ => )*/
                      logger.info("Sending Mail")
                      mailService.sendMail(toRegMail, subj, Some(bodyText), Some(bodyHTML), Some(replytoRegMail), Some(fromRegMail), None, Some(attachmentFiles),
                        None, bccList).map(mailId => {
                        logger.info("Message ID >>> " + mailId)
                        val listofFiles = attachments.toList
                        listofFiles.foreach(sendfile => {
                          sendfile.map(file => {
                            logger.debug("Deleting file >>> " + file.getAbsolutePath)
                            if (file.exists) {
                              file.delete
                            }
                          })

                        })
                      })
                      Ok(Json.obj("Success" -> true, "message" -> "mail request queued"))
                    })
                    })
                  })
                })
              } else {
                Future.apply(Ok(Json.obj("Success" -> false, "Message" -> "given user doesn't exist in database")))
              }
            }
          })
        })
      })
    })
  }
  }


}

