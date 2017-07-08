package service

import javax.inject.{Inject, Singleton}

import constants.{DBConstants, KycConstants, MongoConstants, OrderConstants}
import data.model.Tables.{FceubdRow, FcotptRow, FcsotRow, FcubdRow}
import helpers.{MailHelper, UserHelper, ZendeskHelper}
import models.JsonFormats._
import models._
import org.slf4j.LoggerFactory
import org.zendesk.client.v2.model.{Comment, CustomFieldValue}
import play.api.mvc.Request
import reactivemongo.bson.BSONDocument
import repository.module.{BankRepository, OrderRepository, UserRepository, ZendeskRepository}
import repository.tables._
import service.integration.BSEClientService
import slick.jdbc.MySQLProfile.api._
import utils.DateTimeUtils

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author fincash
  *
  */

@Singleton
class UserService @Inject()(fcultRepo: FcultRepo, fceubdRepo: FceubdRepo, fcubdRepo: FcubdRepo, userRepository: UserRepository, bankRepository: BankRepository, userHelper: UserHelper,
                            mongoDbService: MongoDbService, bSEClientService: BSEClientService, fcbuaRepo: FcbuaRepo, orderService: OrderService, orderRepository: OrderRepository,
                            mailHelper: MailHelper, mailService: MailService, fckycRepo: FckycRepo, fcuaaRepo: FcuaaRepo, configuration: play.api.Configuration,
                            zendeskHelper: ZendeskHelper, zendeskService: ZendeskService, zendeskRepository: ZendeskRepository)
  extends MongoConstants with DBConstants with KycConstants with OrderConstants {


  val logger, log = LoggerFactory.getLogger(classOf[UserService])

  def postUserDataToDb() = {}

  def getUserMobileNo(userId: Long): Future[String] = {
    fcubdRepo.getById(userId).map(ubdRow => {
      if (ubdRow.nonEmpty) {
        ubdRow.head.ubdmobileno.getOrElse("")
      } else {
        ""
      }
    })
  }

  def activateUser(userid: Long, ipAddress: String): Future[(Boolean, String)] = {
    fceubdRepo.getById(userid).flatMap(userRow => {
      if (userRow.nonEmpty) {
        if (userRow.get.eubdisvarified.equalsIgnoreCase(Y_FLAG)) {
          // already verified case returning false.
          logger.debug("Provided user [" + userid + "]  already verified")
          Future.apply((false, "Provided user [" + userid + "]  already verified"))
        } else {
          val updatedUserRow = userRow.get.copy(eubdisvarified = Y_FLAG, modifydate = DateTimeUtils.getCurrentTimeStamp(), lastmodifiedby = OPERATIONS_USER)
          val bseObject = bSEClientService.getBSERegistrationStatus(userid).flatMap(bseStatus => {
            if (!bseStatus) {
              fcultRepo.filter(_.ultubdrfnum === userid).flatMap(ultRow => {
                if (ultRow.nonEmpty) {
                  val username = ultRow.head.ultusername
                  bSEClientService.bseRegistration(userid, ipAddress, Some(username)).map(bseObj => {
                    log.debug("Bse user response ", bseObj)
                    bseObj
                  })
                } else {
                  Future.apply((false, false))
                }
              }).recover {
                case ex: Exception => {
                  logger.error("{}", ex)
                  (false, false);
                }
              }
            } else {
              Future.apply((true, true))
            }
          })
          bseObject.flatMap(bseValue => {
            if (bseValue._1 && bseValue._2) {
              fceubdRepo.updateById(updatedUserRow.id, updatedUserRow).map(value => {
                (true, "Sucessfully activated user in database to verified state");
              }).recover {
                case ex: Exception => (false, "Database exception occurred while updating user status");
              }
            } else {
              Future.apply(false, "Some error occurred in BSE updating user status");
            }
          })
        }

      } else {
        Future.apply(false, "User not available")
      }
    })
  }

  def approveDocuments(userid: Long): Future[Boolean] = {
    fckycRepo.filter(_.kycubdrfnum === userid).flatMap(kycRows => {
      if (kycRows.nonEmpty) {
        val kycUpdatedObj = kycRows.head.copy(kycphotoverification = DOC_APPROVED, kycidverification = DOC_APPROVED, kycsignverification = DOC_APPROVED, modifydate = DateTimeUtils.getCurrentTimeStamp(), lastmodifiedby = OPERATIONS_USER)
        fckycRepo.updateById(userid, kycUpdatedObj).flatMap(retval => {
          fcbuaRepo.filter(_.buaubdrfnum === userid).flatMap(buaRow => {
            if (buaRow.nonEmpty) {
              fcbuaRepo.updateById(buaRow.head.id, buaRow.head.copy(buadocstatus = DOC_APPROVED, modifydate = DateTimeUtils.getCurrentTimeStamp(), lastmodifiedby = OPERATIONS_USER)).flatMap(retval2 => {
                fcuaaRepo.filter(_.uaaubdrfnum === userid).flatMap(uaaRows => {
                  if (uaaRows.nonEmpty) {
                    val uaaFtr = for (uaa <- uaaRows) yield {
                      fcuaaRepo.updateById(uaa.id, uaa.copy(uaadocstatus = DOC_APPROVED, modifydate = DateTimeUtils.getCurrentTimeStamp(), lastmodifiedby = OPERATIONS_USER))
                    }
                    Future.sequence(uaaFtr).map(values => {
                      true
                    })
                    /*fcuaaRepo.updateById(uaaRows.head.id, uaaRows.head.copy(uaadocstatus = DOC_APPROVED)).map(retval3 => {
                      fcuaaRepo.updateById(uaaRows.last.id, uaaRows.last.copy(uaadocstatus = DOC_APPROVED, modifydate = DateTimeUtils.getCurrentTimeStamp(), lastmodifiedby = OPERATIONS_USER))
                      true
                    })*/
                  }
                  else {
                    Future.apply(false)
                  }
                })
              })
            }
            else {
              Future.apply(false)
            }
          })
        })
      }
      else {
        Future.apply(false)
      }
    })
  }

  def approvePhoto(userid: Long): Future[Boolean] = {
    fckycRepo.filter(_.kycubdrfnum === userid).flatMap(kycRow => {
      if (kycRow.nonEmpty) {
        var kycrow = kycRow.head
        kycrow = kycrow.copy(kycphotoverification = DOC_APPROVED)
        fckycRepo.updateById(userid, kycrow).map(retval => {
          true
        })
      }
      else {
        Future.apply(false)
      }
    })
  }

  def bseRegistration(userid: Long, ipAddress: String): Future[(Boolean, Boolean)] = {
    fceubdRepo.getById(userid).flatMap(userRow => {
      if (userRow.nonEmpty) {
        if (userRow.get.eubdisvarified.equalsIgnoreCase(Y_FLAG)) {
          // already verified case returning false.
          logger.debug("Provided user [" + userid + "]  already BSE Registered")
          Future.apply(true, true)
        } else {
          bSEClientService.getBSERegistrationStatus(userid).flatMap(bseStatus => {
            if (!bseStatus) {
              fcultRepo.filter(_.ultubdrfnum === userid).flatMap(ultRow => {
                if (ultRow.nonEmpty) {
                  val username = ultRow.head.ultusername
                  bSEClientService.bseRegistration(userid, ipAddress, Some(username)).map(bseObj => {
                    log.debug("Bse user response ", bseObj)
                    bseObj
                  })
                } else {
                  Future.apply((false, false))
                }
              })
            } else {
              Future.apply((true, true))
            }
          })
        }
      } else {
        Future.apply(false, false)
      }
    })
  }

  def getEUBDbyPk(userid: Long): Future[Option[FceubdRow]] = {
    fceubdRepo.getById(userid).map(row => {
      row
    })
  }

  def getUsernameFromRequest(request: Request[Any]): Future[Option[String]] = {
    getUserObjectFromReq(request).map(value => {
      if (value.nonEmpty) {
        value.get.username
      } else {
        None
      }
    })
  }

  def getUseridFromRequest(request: Request[Any]): Future[Option[Long]] = {
    getUserObjectFromReq(request).map(value => {
      if (value.nonEmpty) {
        value.get.userid
      } else {
        None
      }
    })
  }

  def getUserObjectFromReq(request: Request[Any]): Future[Option[UserLoginObject]] = {
    val token = getTokenFromRequest(request)
    if (token.isEmpty) {
      return Future.apply(None)
    }
    val findquery = BSONDocument("token" -> token)
    //var username = "";
    collection(TOKEN_COLLECTION_NAME).flatMap { coll =>
      coll.find(findquery)
        .one[BSONDocument].map { doc =>
        if (doc.nonEmpty) {
          val document = doc.get
          Some(new UserLoginObject(document.getAs[String]("username"), document.getAs[Long]("userid"), document.getAs[Long]("foliocount"), document.getAs[String]("firstname"), document.getAs[String]("pan"), document.getAs[String]("mobile"), None))
          //username
        } else {
          None
        }
      }
    }
  }

  def collection(name: String) = mongoDbService.collection(name)

  def getTokenFromRequest(request: Request[Any]): Option[String] = {
    request.headers.get("Authorization").map { authHeader =>
      if (authHeader.nonEmpty) {
        return Some(authHeader.replaceFirst("Bearer ", ""))
      }
      return None
    }
  }

  /**
    * This method is used to create a new user
    *
    * @param data
    * @return
    */

  def getUserByUserName(userName: String): Future[Option[FcubdRow]] = {
    userRepository.getUserByUsername(userName)
  }

  def getUserNomineeDetails(userName: String): Future[Option[String]] = {
    userRepository.getUserNomineeDetails(userName).map(nomineeList => {
      val nomineeFutureList = for (nominee <- nomineeList) yield {
        userHelper.getNomineeLabel(nominee._1, nominee._2)
      }
      nomineeFutureList.headOption
    })
  }

  def getUserBankDetails(userName: String): Future[List[String]] = {
    userRepository.getUserBankDetails(userName).map(banksList => {
      for (bank <- banksList) yield {
        bank.bmtbankname
      }
    })
  }

  def saveOTPDetails(userId: Long, otp: String, gatewayId: String, purpose: String, mobileNo: String, ip: String): Future[Long] = {
    userRepository.saveOTPDetails(userId, otp, gatewayId, purpose, mobileNo, ip)
  }

  def updateOTPMessageId(messageId: String, otprfnum: Long, userName: String): Future[Int] = {
    userRepository.updateOTPMessageId(messageId, otprfnum, userName)
  }

  def validateOTP(otp: String, purpose: String, userId: Long): Future[Option[FcotptRow]] = {
    userRepository.validateOTP(otp, purpose, userId).map(_.headOption);
  }

  def updateOTPStatus(messageId: String, status: String): Future[Int] = {
    userRepository.updateOTPStatus(messageId, status)
  }

  def getMessageStatus(messageId: String): Future[String] = {
    userRepository.getMessageStatus(messageId)
  }

  def sendRegistrationStatusMail(userId: Long): Future[Boolean] = {
    val heading = PropertiesLoaderService.getConfig().getString("mail.registrationStatus.heading")
    val subj = PropertiesLoaderService.getConfig().getString("mail.registrationStatus.subject")
    val from = this.mailHelper.fromRegStatMail
    val replyto = this.mailHelper.replytoReStatgMail
    val bcc = this.mailHelper.bccRegStat

    fcubdRepo.getById(userId).map(ubdRowOpion => {
      if (ubdRowOpion.nonEmpty) {
        val ubdRow = ubdRowOpion.get
        val userName = ubdRowOpion.get.ubdemailid

        val mailHeaderTemplate = views.html.mailHeader(heading, mailHelper.getMth)
        val mailBodyTemplate = views.html.registrationStatus(ubdRow, mailHelper.getMth)
        val mailTemplate = views.html.mail(mailHeaderTemplate, mailBodyTemplate, mailHelper.getMth)
        val bodyText = views.html.registrationStatusTxt(ubdRow, mailHelper.getMth).toString()
        val bodyHTML = mailTemplate.toString()

        var bccList: Option[ListBuffer[String]] = None
        if (bcc != null && bcc.trim.length > 0) {
          val bccl = ListBuffer[String]()
          bccl.+=(bcc)
          bccList = Some(bccl)
        }

        mailService.sendMail(userName, subj, Some(bodyText), Some(bodyHTML), None, Some(from), None, None,
          None, bccList).map(mailId => {
          logger.info("Reg Status Message ID >>> " + mailId)

        })
        true

      } else {
        false
      }
    })
  }

  /*ekyc Status mail*/

  def sendeKYCMail(userId: Long, eKYCstatus: Boolean): Future[Boolean] = {

    val bcceKYC = this.mailHelper.bcceKYC
    fcubdRepo.getById(userId).map(ubdRowOpion => {
      if (ubdRowOpion.nonEmpty) {
        val ubdRow = ubdRowOpion.get
        val email = ubdRow.ubdemailid

        val heading = PropertiesLoaderService.getConfig().getString("mail.eKYC-status.heading")
        val subj = PropertiesLoaderService.getConfig().getString("mail.eKYC-status.subject")

        val mailHeaderTemplate = views.html.mailHeader(heading, mailHelper.getMth)
        val mailBodyTemplate = views.html.kycStatus(ubdRow, eKYCstatus, mailHelper.getMth)
        val mailTemplate = views.html.mail(mailHeaderTemplate, mailBodyTemplate, mailHelper.getMth)

        val bodyText = views.html.kycStatusTxt(ubdRow, eKYCstatus, mailHelper.getMth).toString()
        val bodyHTML = mailTemplate.toString()

        var bccList: Option[ListBuffer[String]] = None
        if (bcceKYC != null && bcceKYC.trim.length > 0) {
          val bcc = ListBuffer[String]()
          bcc.+=(bcceKYC)
          bccList = Some(bcc)
        }
        mailService.sendMail(email, subj, Some(bodyText), Some(bodyHTML), None, None, None, None,
          None, bccList).map(mailId => {
          logger.info("kyc -- Message ID >>> " + mailId)
        })
        true
      }
      else {
        false
      }
    })

  }


  def sendISIPMandateMail(subOrderId: Long, toState: Long): Future[String] = {
    orderRepository.getSubOrderDetails(subOrderId).flatMap(subOrderSeq => {
      val sotRow = subOrderSeq.head
      val userId = sotRow.sotubdrfnum
      fcubdRepo.getById(userId).flatMap(ubdRowOption => {
        val ubdRow = ubdRowOption.get
        val email = ubdRowOption.get.ubdemailid
        var userObj: UserLoginObject = UserLoginObject(Some(email), Some(userId), None, None, None, None, None)
        orderService.populateOrderDetails(sotRow.sotomtrfnum, userObj).flatMap(orderDetails => {
          var urnMap = Map[Long, String]();
          val subOrderList: List[SubOrderDetails] = orderDetails.subOrderDetails.filter(so => so.subOrderId == subOrderId);
          bankRepository.getSubOrderMandateDetails(subOrderList.head.subOrderId).flatMap(mmtRows => {
            if (mmtRows.nonEmpty) {
              bankRepository.getBmtByBuaRfnum(mmtRows.head.mmtbuarfnum).flatMap(bmtRow => {
                urnMap += (subOrderList.head.subOrderId -> mmtRows.head.mmtexternalid.getOrElse(""))
                val from = configuration.underlying.getString("mail.order-placed.from")
                val replyto = configuration.underlying.getString("mail.order-placed.from")
                val bccOrder = configuration.underlying.getString("mail.order-placed.bcc")
                var bccList: Option[ListBuffer[String]] = None
                if (bccOrder != null && bccOrder.trim.length > 0) {
                  val bcc = ListBuffer[String]()
                  bcc.+=(bccOrder)
                  bccList = Some(bcc)
                }

                if (toState == ORDER_COMPLETED) {
                  val heading = PropertiesLoaderService.getConfig().getString("mail.isip-mandate.heading")
                  val subj = PropertiesLoaderService.getConfig().getString("mail.isip-mandate.subject")
                  val mailHeaderTemplate = views.html.mailHeader(heading, mailHelper.getMth)
                  val mailBodyTemplate = views.html.orderSIP(ubdRow, bmtRow, true, subOrderList, urnMap, mailHelper.getMth)
                  val mailTemplate = views.html.mail(mailHeaderTemplate, mailBodyTemplate, mailHelper.getMth)
                  val bodyText = views.html.orderSIPTxt(ubdRow, bmtRow, true, subOrderList, urnMap, mailHelper.getMth).toString()
                  val bodyHTML = mailTemplate.toString()
                  mailService.sendMail(email, subj, Some(bodyText), Some(bodyHTML), None, Some(from), None, None,
                    None, None).flatMap(mailId => {
                    logger.info("SIP order -- Message ID >>> " + mailId)
                    updateOrderTicketForISIPMail(userId, email, sotRow, "ISIP mail sent successfully").map(res => {
                      "ISIP mail sent susuccessfully"
                    })
                  })

                } else if (toState == ORDER_REVERSED) {
                  val heading = PropertiesLoaderService.getConfig().getString("mail.isip-mandate.reversal.heading")
                  val subj = PropertiesLoaderService.getConfig().getString("mail.isip-mandate.reversal.subject")
                  val mailHeaderTemplate = views.html.mailHeader(heading, mailHelper.getMth)
                  val mailBodyTemplate = views.html.reverseOrderSIP(ubdRow, true, subOrderList, urnMap, mailHelper.getMth)
                  val mailTemplate = views.html.mail(mailHeaderTemplate, mailBodyTemplate, mailHelper.getMth)
                  val bodyText = views.html.reverseOrderSIPTxt(ubdRow, true, subOrderList, urnMap, mailHelper.getMth).toString()
                  val bodyHTML = mailTemplate.toString()
                  mailService.sendMail(email, subj, Some(bodyText), Some(bodyHTML), None, Some(from), None, None,
                    None, None).flatMap(mailId => {
                    logger.info("SIP order -- Message ID >>> " + mailId)
                    updateOrderTicketForISIPMail(userId, email, sotRow, "ISIP reversal mail sent successfully").map(res => {
                      "ISIP reversal mail sent successfully!"
                    })
                  })
                } else {
                  Future.apply("Unknown toState for subOdrer!");
                }
              })
            } else {
              Future.apply("mandate not found!")
            }
          })
        })
      })
    })
  }

  def updateOrderTicketForISIPMail(userid: Long, username: String, sotRow: FcsotRow, commentText: String): Future[AnyRef] = {
    zendeskRepository.getTicketByUserIdANDorderId(userId = userid, subOrderId = sotRow.id).flatMap(tickets => {
      val tkt = tickets.headOption;
      if (tkt.isDefined) {
        val comment = new Comment()
        comment.setBody(commentText);
        comment.setPublic(false);
        Future.apply(zendeskService.setTktComment(tkt.get.tktticketid.toLong, comment));
      } else {
        val requester = zendeskHelper.createRequester()
        requester.setEmail(username)
        requester.setName(username)
        val comment = new Comment()
        comment.setBody(commentText)
        comment.setPublic(false)
        val grpAndPurpose = zendeskHelper.getGroupANDPurposeFromOrderType(sotRow.sottype, sotRow.sotinvestmentmode);
        zendeskService.createTicket(requester, "ISIP_Mail_SubOrder-" + sotRow.id, Some(comment), Some(grpAndPurpose._1), username, username, userid, Some(sotRow.id), None)
      }
    })
  }

  def updateMobileNumber(username: String, userid: Long, mob: String): Future[Boolean] = {
    userRepository.saveMobNumber(userid, mob).flatMap(isUpdated => {
      if (isUpdated) {
        val istktAllowed = zendeskHelper.isTktAllowed
        if (istktAllowed) {
          var customField = new CustomFieldValue(zendeskHelper.ZENDESK_TICKET_USER_STAGE, zendeskHelper.ZENDESK_TICKET_VALUE_MOBILE_VERIFIED)
          val customFieldList = new java.util.ArrayList[CustomFieldValue]
          customFieldList.add(customField)
          val purpose = Some(zendeskHelper.TKT_PURPOSE_REGISTRATION)
          zendeskRepository.getTicketsByUserIdANDpurpose(userid,purpose).flatMap(fctktRows =>{
            if(fctktRows.nonEmpty && !fctktRows.head.tktticketid.equalsIgnoreCase("0")){
              Future.apply(Some(fctktRows.head.tktticketid.toLong))
            }else {
              var oldTktrfnum : Option[Long] = None
              if(fctktRows.nonEmpty){
                oldTktrfnum = Some(fctktRows.head.id)
              }
              val requester = zendeskHelper.createRequester
              requester.setName(username)
              requester.setEmail(username)
              zendeskService.createTicket(requester, zendeskHelper.onBoardingSubject, None, Some(zendeskHelper.ZENDESK_GROUP_ONBOARDING), username, username, userid,None,purpose, oldTktrfnum).map(tktIdOption =>{
                tktIdOption
              })
            }
          }).map(tktOption=>{
            if(tktOption.nonEmpty){
              val tktId = tktOption.get
              logger.debug("zendesk ticket id ["+tktId+"]")
              val customFieldRes = zendeskService.setCustomField(tktId, customFieldList, None, None, None)
              logger.debug("Customfield Status -->"+ customFieldRes)
              if(customFieldRes._2){
                val zdUserId = zendeskHelper.getUserIdByEmail(username)
                if (zdUserId > 0) {
                  zendeskService.updateUserPhone(zdUserId, mob)
                }
              }
            }else{
              logger.debug("zendesk ticket id not recieved")
            }
           true
          })
        } else {
          Future.apply(true);
        }
      } else {
        Future.apply(false);
      }
    })

  }

  def getUserByPan(pan: String): Future[Seq[FcubdRow]] = {
    userRepository.getAllUsersByPan(pan);
  }
}