package service

import java.io.{File, FileInputStream}
import java.util
import java.util.Calendar
import javax.inject.{Inject, Singleton}

import constants.{CNDConstants, KycConstants, OrderConstants, ZendeskConstants}
import data.model.Tables.FctktRow
import helpers.{MailHelper, ZendeskHelper}
import models.{OrderDetails, SubOrderDetails, SubOrderUnitDetails}
import org.slf4j.LoggerFactory
import org.zendesk.client.v2.model.Ticket.Requester
import org.zendesk.client.v2.model._
import play.api.libs.json.{JsObject, JsValue}
import repository.module.{KycRepository, OrderRepository, UserRepository, ZendeskRepository}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}


/**
  * Created by Fincash on 22-05-2017.
  */
@Singleton
class ZendeskService @Inject()(implicit ec: ExecutionContext, zendeskHelper: ZendeskHelper, userRepository: UserRepository, kycRepository: KycRepository, orderRepository: OrderRepository,
                               zendeskRepository: ZendeskRepository, configuration: play.api.Configuration, mailHelper: MailHelper,
                               bankService: BankService) extends CNDConstants with KycConstants with OrderConstants with ZendeskConstants {

  val IS_ZENDESK_CLIENT_POSTING_ALLOWED = configuration.underlying.getBoolean("zendesk.tktAllowed")
  val logger, log = LoggerFactory.getLogger(classOf[ZendeskService])
  val zendesk: org.zendesk.client.v2.Zendesk = zendeskHelper.zendesk


  def createTicket(requester: Ticket.Requester, subject: String, comment: Option[Comment] = None, groupId: Option[Long] = None,
                   recipient: String, username: String, userid: Long, subOrderId: Option[Long], purpose: Option[Long] = None,
                   dbTicketId: Option[Long] = None): Future[Option[Long]] = {

    val ticket = zendeskHelper.createTicketObject()
    ticket.setRequester(requester)
    ticket.setSubject(subject)
    val commentObject = new Comment()
    commentObject.setBody("comment was not set while creating this ticket.")
    commentObject.setPublic(false)
    ticket.setComment(comment.getOrElse(commentObject))
    ticket.setGroupId(groupId.get)
    ticket.setRecipient(recipient)

    logger.debug(" group " + groupId.get + " subject " + subject + " requester email [" + requester.getEmail + "] requester name [" + requester.getName + "]")

    if (IS_ZENDESK_CLIENT_POSTING_ALLOWED) {
      val status = Status.NEW.toString.toLowerCase()
      val tktSource = zendeskHelper.ZENDESK_SOURCE_API.toLowerCase()
      var fctktRowFut: Future[Option[FctktRow]] = Future.apply(None)
      if (dbTicketId.nonEmpty) {
        logger.debug("Old ticket to be used TKTRFNUM [" + dbTicketId.get + "]")
        fctktRowFut = zendeskRepository.getTicketById(dbTicketId.get).map(fctktRow => {
          fctktRow
        }) recover {
          case ex => logger.error("{}", ex)
            None
        }
      } else {
        logger.debug("New ticket TKTRFNUM to be created")
        fctktRowFut = zendeskRepository.saveTicket("0", purpose, userid, username, status, tktSource, subOrderId).map(fctktRow => {
          fctktRow
        }) recover {
          case ex => logger.error("{}", ex)
            None
        }
      }
      fctktRowFut.map(fctktRow => {
        if (fctktRow.nonEmpty) {
          val tktId = zendesk.createTicket(ticket).getId.toString
          val updatedFctktRow = fctktRow.get.copy(tktticketid = tktId)
          zendeskRepository.updateZendeskTicket(updatedFctktRow)
          Some(tktId.toLong)
        } else {
          None
        }
      }) recover {
        case e: Exception => {
          logger.error("{}", e)
          None
        }
      }

    } else {
      Future.apply(None)
    }

  }

  def updateStatus(tktId: Long, status: Status, commentString: Option[String] = None): Future[Boolean] = {
    try {
      if (IS_ZENDESK_CLIENT_POSTING_ALLOWED) {
        val zdTicket = zendesk.getTicket(tktId)
        val fromStatus = zdTicket.getStatus.toString
        zdTicket.setStatus(status)
        if (commentString.nonEmpty) {
          val comment = new Comment()
          comment.setBody(commentString.get)
          comment.setPublic(false)
          zdTicket.setComment(comment)
        }
        val tkt: Ticket = zendesk.updateTicket(zdTicket)
        logger.debug("Ticket Id [" + tkt.getId + "] updated successfully @ [" + tkt.getUpdatedAt + "]")
        if (tkt.getStatus == status) {
          zendeskRepository.updateTicketStatus(tktId, fromStatus, status.toString).map(value => {
            value
          }) recover {
            case e: Exception => {
              logger.error("{}", e)
              false
            }
          }
        } else {
          Future.apply(false)
        }
      } else {
        Future.apply(false)
      }
    } catch {
      case e: Exception => {
        logger.error("{}", e)
        Future.apply(false)
      }
    }
  }

  def getTicket(ticketId: Long): Option[Ticket] = {
    try {
      Some(zendesk.getTicket(ticketId))
    } catch {
      case ex: Exception => {
        logger.error("{}", ex)
        None
      }
    }
  }

  def setCustomField(tktId: Long, customFieldList: util.List[CustomFieldValue], requester: Option[Requester], commentString: Option[String], commentPublic: Option[Boolean]): (Long, Boolean) = {
    try {
      if (IS_ZENDESK_CLIENT_POSTING_ALLOWED) {
        val zdTicket = zendesk.getTicket(tktId)
        zdTicket.setCustomFields(customFieldList)
        if (commentString.nonEmpty && commentString.get.trim.length > 0) {
          val commenTs = new Comment()
          commenTs.setBody(commentString.get)
          if (commentPublic.nonEmpty) {
            commenTs.setPublic(commentPublic.get)
          } else {
            commenTs.setPublic(false)
          }
          zdTicket.setComment(commenTs)
        }
        if (requester.nonEmpty) {
          zdTicket.setRequester(requester.get)
        }
        val ticket: org.zendesk.client.v2.model.Ticket = zendesk.updateTicket(zdTicket)
        logger.debug("Ticket Id [" + ticket.getId + "] updated successfully @ [" + ticket.getUpdatedAt + "]")
        (ticket.getId, true)
      } else {
        (0L, false)
      }
    } catch {
      case ex: Exception => {
        logger.error("{}", ex)
        (0L, false)
      }
    }
  }

  def updateUserPhone(zdUserId: Long, mob: String): Boolean = {
    try {
      if (IS_ZENDESK_CLIENT_POSTING_ALLOWED) {
        val user = zendesk.getUser(zdUserId)

        user.setPhone(mob)
        val updatedUser = zendesk.updateUser(user)
        logger.debug("user update [" + updatedUser.getId + "] updated successfully @ [" + updatedUser.getUpdatedAt + "]")
        updatedUser.getPhone.equalsIgnoreCase(mob)
      } else {
        false
      }
    } catch {
      case ex: Exception => {
        logger.error("{}", ex)
        false
      }
    }
  }

  def setUserField(zdUserId: Long, userField: java.util.Map[String, Object]): Boolean = {
    try {
      if (IS_ZENDESK_CLIENT_POSTING_ALLOWED) {
        val user = zendesk.getUser(zdUserId)
        user.setUserFields(userField)
        val updatedUser = zendesk.updateUser(user)
        logger.debug("user update [" + updatedUser.getId + "] updated successfully @ [" + updatedUser.getUpdatedAt + "]")
        val userFieldsKeys = updatedUser.getUserFields.keySet
        userFieldsKeys.size.equals(userField.size())
      } else {
        false
      }
    } catch {
      case ex: Exception => {
        logger.error("{}", ex)
        false
      }
    }
  }

  def setUserName(zdUserId: Long, name: String): Boolean = {
    try {
      val user = zendesk.getUser(zdUserId)
      user.setName(name)
      val updatedUser = zendesk.updateUser(user)
      logger.debug("user update [" + updatedUser.getId + "] updated successfully @ [" + updatedUser.getUpdatedAt + "]")
      updatedUser.getName.equalsIgnoreCase(name)
    } catch {
      case ex: Exception => {
        logger.error("{}", ex)
        false
      }
    }
  }

  /**
    *
    * @param tktId
    * @param comment
    * @param requester
    * @return tuple of ticketId & Status (true/false)
    */
  def setTktComment(tktId: Long, comment: Comment, requester: Option[Requester] = None): (Long, Boolean) = {
    try {
      val zdTicket = zendesk.getTicket(tktId)
      if (requester.nonEmpty) {
        zdTicket.setRequester(requester.get)
      }
      zdTicket.setComment(comment)
      val ticket = zendesk.updateTicket(zdTicket)
      logger.debug("Ticket Id [" + ticket.getId + "] updated successfully @ [" + ticket.getUpdatedAt + "]")
      (ticket.getId, true)
    } catch {
      case ex: Exception => {
        logger.error("{}", ex)
        (tktId, false)
      }
    }
  }

  /**
    *
    * @param orderDetails
    * @param subOrder
    * @param fromState
    * @param userId
    * @return SuborderId & TicketId tuple
    */

  def postOrderUpdate(orderDetails: OrderDetails, subOrder: SubOrderDetails, fromState: String, fromStateCode: String, userId: Long): Future[(Long, Long)] = {

    userRepository.getUserByPk(userId).flatMap(ubdRowList => {
      val ubdRow = ubdRowList.head
      val userName = ubdRow.ubdemailid
      logger.debug("Zendesk Ticket posting allowed [" + IS_ZENDESK_CLIENT_POSTING_ALLOWED + "]")
      if (IS_ZENDESK_CLIENT_POSTING_ALLOWED) {
        val requester = zendeskHelper.createRequester
        val userEmail = userName
        requester.setEmail(userEmail)
        requester.setName(ubdRow.ubdfirstname.getOrElse(userName))

        (for {
          mandateBankDetail <- bankService.getMandateBankDetails(subOrder)
          mandateDetails <- bankService.getMandate(subOrder)
        } yield {
          var mandateAmount: Option[Double] = None
          if (mandateDetails.nonEmpty) {
            mandateAmount = Some(mandateDetails.get.mmtamount)
          }
          val subOrderExt = subOrder.extDetails
          var bseSchemeCode: Option[String] = None
          var estimatedAllotmentDate: Option[String] = None
          var cancellationDate: Option[String] = None
          var folioNo: Option[String] = None
          for (childDetail <- subOrderExt.get) {

            if (childDetail.extDetailKey == BSE_SCHEME_CODE_KEY) {
              bseSchemeCode = Some(childDetail.extDetailValue)
            }
            if (childDetail.extDetailKey == ORDER_APPROX_ALLOT_KEY) {
              estimatedAllotmentDate = Some(childDetail.extDetailValue)
            }
            if (childDetail.extDetailKey == ORDER_CANCEL_CUT_OFF_KEY) {
              cancellationDate = Some(childDetail.extDetailValue)
            }
            if (childDetail.extDetailKey == FOLIO_NO_KEY) {
              folioNo = Some(childDetail.extDetailValue)
            }
          }
          logger.debug("getMandateBankDetails [" + mandateBankDetail + "] bseSchemeCode [" + bseSchemeCode + "]")
          zendeskRepository.getTicketByOrderId(subOrder.subOrderId).flatMap(tktRows => {
            if (tktRows.nonEmpty) {
              val tktRow = tktRows.head
              zendeskRepository.updateTicketInDatabase(tktRow.id, userName).map(dbUpdated => {
                try {
                  logger.debug("Modify date in db updated " + dbUpdated)
                  val zdTicket = zendesk.getTicket(tktRow.tktticketid.toLong)
                  val currentCal = Calendar.getInstance()
                  val updateCal = Calendar.getInstance()
                  // for comments to reapper 30 seconds delay in  updates is required as it might mean code is transferring order from one state to another. Might results in too many comments being posted
                  currentCal.add(Calendar.SECOND, -30)
                  updateCal.setTimeInMillis(tktRow.modifydate.getTime)
                  logger.debug(" update cal [" + updateCal.getTime + "] currentCal [" + currentCal.getTime + "] TKTMODIFIEDDATE [" + tktRow.modifydate + "]")
                  if (subOrder.buySellType.equalsIgnoreCase(BUYSELL_SELL) || updateCal.getTime.before(currentCal.getTime)) {
                    val bodyTextZd = views.html.zdOrderProcessedTxt(ubdRow, mandateBankDetail, mandateAmount, orderDetails, subOrder, bseSchemeCode.getOrElse(""),
                      Some(fromState), Some(fromStateCode), estimatedAllotmentDate, cancellationDate, folioNo, mailHelper.getMth).toString()
                    val comment = new Comment()
                    comment.setBody(bodyTextZd)
                    comment.setPublic(false)
                    zdTicket.setComment(comment)
                  }
                  val customField1 = new CustomFieldValue(zendeskHelper.ZENDESK_TICKET_ORDER_STATUS, subOrder.stateName.getOrElse("NA"))
                  val customFieldList = new java.util.ArrayList[CustomFieldValue]
                  customFieldList.add(customField1)
                  zdTicket.setCustomFields(customFieldList)
                  zdTicket.setRequester(requester)
                  val updatedTicket = zendesk.updateTicket(zdTicket)
                  logger.debug("Ticket Id [" + updatedTicket.getId + "] updated successfully @ [" + updatedTicket.getUpdatedAt + "]")
                  (subOrder.subOrderId, updatedTicket.getId.toString.toLong)
                } catch {
                  case ex: Exception => {
                    logger.debug("Error updating ticket " + ex.getMessage)
                    (subOrder.subOrderId, 0L)
                  }
                }
              })
            } else {
              logger.debug("@@no ticket found with order Id [" + subOrder.subOrderId + "]")
              //var tickets = Future.sequence(for(subOrder <- subOrderDetail.subOrderDetails) yield {

              val bodyTextZd = views.html.zdOrderProcessedTxt(ubdRow, mandateBankDetail, mandateAmount, orderDetails, subOrder,
                bseSchemeCode.getOrElse(""), Some(fromState), Some(fromStateCode), estimatedAllotmentDate, cancellationDate, folioNo, mailHelper.getMth).toString()
              val comment = new Comment()
              comment.setBody(bodyTextZd)
              comment.setPublic(false)
              var tktSubject = orderDetails.orderId.toString + '/' + subOrder.subOrderId.toString
              tktSubject += "--" + subOrder.schemeName.getOrElse("") + '-' + subOrder.schemePlan.getOrElse("") + '-' + subOrder.schemeOption.getOrElse("")
              val resTuple = zendeskHelper.getGroupANDPurposeFromOrderType(orderDetails.orderType, subOrder.investmentMode)
              val assigneeGroup = resTuple._1
              val tktPurpose = resTuple._2
              logger.debug("Assignee group [" + assigneeGroup + "] tktPupose [" + tktPurpose + "] requester [" + requester + "] tktSubject [" + tktSubject + "] username [" + userName + "] userid [" + userId + "]")
              createTicket(requester, tktSubject, Some(comment), Some(assigneeGroup), userEmail, userEmail, userId, Some(subOrder.subOrderId), Some(tktPurpose), None).flatMap(_tktId => {
                logger.debug("createTicket [" + _tktId + "]")
                if (_tktId.nonEmpty) {
                  val customField1 = new CustomFieldValue(zendeskHelper.ZENDESK_TICKET_USER_STAGE, zendeskHelper.ZENDESK_TICKET_VALUE_ACCOUNT_ACTIVE)
                  val customField2 = new CustomFieldValue(zendeskHelper.ZENDESK_TICKET_ORDER_STATUS, subOrder.stateName.getOrElse("NA"))
                  kycRepository.getUserKYCStatus(userId).map(retList => {
                    try {
                      if (retList.nonEmpty && retList.length > 0) {
                        val kycrow = retList.head
                        val kycStatus = kycrow.kycstatus
                        var zdkycstatus = ""
                        val commentString = kycrow.kyctype
                        if (kycStatus.equalsIgnoreCase(KYC_DONE)) {
                          zdkycstatus = zendeskHelper.ZENDESK_TICKET_VALUE_KYC_DONE
                        } else if (kycStatus.equalsIgnoreCase(KYC_EXTERNALLY_DONE)) {
                          zdkycstatus = zendeskHelper.ZENDESK_TICKET_VALUE_AADHAAR_EKYC
                        } else if (kycStatus.equalsIgnoreCase(KYC_NOTDONE)) {
                          zdkycstatus = zendeskHelper.ZENDESK_TICKET_VALUE_KYC_NOT_DONE
                        } else if (kycStatus.equalsIgnoreCase(KYC_UNDERPROCESS)) {
                          zdkycstatus = zendeskHelper.ZENDESK_TICKET_VALUE_KYC_UNDER_PROCESS
                        }
                        val customField3 = new CustomFieldValue(zendeskHelper.ZENDESK_TICKET_KYC_STATUS, zdkycstatus)
                        val customFieldList = new java.util.ArrayList[CustomFieldValue]
                        customFieldList.add(customField1)
                        customFieldList.add(customField2)
                        customFieldList.add(customField3)
                        val zdTicket = getTicket(_tktId.get)
                        if (zdTicket.nonEmpty) {
                          val ticket = zdTicket.get
                          ticket.setRequester(requester)
                          ticket.setCustomFields(customFieldList)
                          val comments = new Comment()
                          var commentStr = "KYC TYPE: " + commentString.get
                          /*commentStr  = commentStr + "\n" + " Order Snapshot "*/
                          comments.setBody(commentStr)
                          val url = mailHelper.filepath + "/" + orderDetails.snapshotPath.get
                          mailHelper.downLoadFile(url, "png").map(_file => {
                            var contentType = "image/png"
                            val fin = new FileInputStream(_file.getAbsoluteFile)
                            val cnt = fin.available
                            val bytes = Array.ofDim[Byte](cnt)
                            fin.read(bytes)
                            fin.close()
                            val token = createUpload(_file, contentType, bytes)
                            val zdTokens = new java.util.ArrayList[String]
                            if (token.nonEmpty) {
                              zdTokens.add(token.get)
                            }
                            if (zdTokens.size > 0) {
                              comments.setUploads(zdTokens)
                            }
                            comments.setPublic(false)
                            ticket.setComment(comments)
                            try {
                              val updateTicket = zendesk.updateTicket(ticket)
                              logger.debug("Ticket Id [" + updateTicket.getId + "] updated successfully @ [" + updateTicket.getUpdatedAt + "]")
                            } catch {
                              case exp: Exception => {
                                logger.error("Error updating ticket " + _tktId.get)
                              }
                            }
                            if (_file.exists) {
                              _file.delete
                            }
                          })
                        }
                      }
                      logger.debug("ticketId [" + _tktId + "]")
                      (subOrder.subOrderId, _tktId.getOrElse(0L))
                    } catch {
                      case e: Exception => {
                        logger.error("{}", e)
                        (subOrder.subOrderId, 0L)
                      }
                    }
                  })
                } else {
                  Future.apply((subOrder.subOrderId, 0L))
                }

              })
            }
          })
        }).flatMap(value => value)
      } else {
        logger.debug("@@tkt update not allowed for this config")
        Future.apply((subOrder.subOrderId, 0L))
      }
    })
  }

  def postOrderUnitUpdate(subOrderUnitDetails: SubOrderUnitDetails): Future[Boolean] = {
    if (IS_ZENDESK_CLIENT_POSTING_ALLOWED) {
      val subOrderId = subOrderUnitDetails.subOrderId
      zendeskRepository.getTicketByOrderId(subOrderId).map(tktRows => {
        if (tktRows.nonEmpty) {
          val tktRow = tktRows.head
          val zdTicket = zendesk.getTicket(tktRow.tktticketid.toLong)
          val customField1 = new CustomFieldValue(zendeskHelper.ZENDESK_TICKET_ORDER_STATUS, subOrderUnitDetails.orderStateName.getOrElse(""))
          val customFieldList = new java.util.ArrayList[CustomFieldValue]
          customFieldList.add(customField1)
          val tktComment = new Comment()
          val bodyTextZd = views.html.zdOrderUnitDetailsTxt(subOrderUnitDetails, mailHelper.getMth).toString()
          tktComment.setBody(bodyTextZd)
          tktComment.setPublic(false)
          zdTicket.setCustomFields(customFieldList)
          zdTicket.setComment(tktComment)
          zendeskRepository.updateTicketInDatabase(tktRow.id, "SYSTEM").map(done => {
            logger.debug("Fctkt row modification successfull")
          }).recover {
            case e: Exception => {
              logger.error("{}", e)
            }
          }
          val updateTicket = zendesk.updateTicket(zdTicket)
          logger.debug("Ticket Id [" + updateTicket.getId + "] updated successfully @ [" + updateTicket.getUpdatedAt + "]")
          true
        } else {
          logger.debug("No Ticket found  for given order")
          false
        }
      }).recover {
        case ex: Exception => {
          logger.error("{}", ex)
          false
        }
      }
    } else {
      logger.debug("@@tkt update not allowed for this config")
      Future.apply(false)
    }
  }

  def createUpload(file: File, contentType: String, bytes: Array[Byte]): Option[String] = {
    this.zendeskHelper.createUpload(file, contentType, bytes)
  }


  /**
    *
    * @param userid
    * @param username
    * @param purpose
    * @param comment
    * @return
    */

  def updateTicketsOnUserVerification(userid: Long, purpose: Option[Long], comment: Option[String]): Future[Boolean] = {
    //val purpose = Some(zendeskHelper.TKT_PURPOSE_KYC)
    val customField: CustomFieldValue = new CustomFieldValue(zendeskHelper.ZENDESK_TICKET_USER_STAGE, zendeskHelper.ZENDESK_TICKET_VALUE_ACCOUNT_ACTIVE)
    val customFieldList = new java.util.ArrayList[CustomFieldValue]
    customFieldList.add(customField)
    if (zendeskHelper.isTktAllowed) {
      zendeskRepository.getTicketsByUserIdANDpurpose(userid, purpose).map(fctktRows => {
        val retvalList = ListBuffer[Long]()
        if (fctktRows.nonEmpty) {
          for (tktRow <- fctktRows) {
            if (!tktRow.tktticketid.equalsIgnoreCase("0"))
              retvalList.+=(tktRow.tktticketid.toLong)
          }
        }
        if (retvalList.length > 0) {
          retvalList.foreach(tktId => {
            logger.debug("fetching ticket id >> " + tktId)
            val zdTicketOpt = getTicket(tktId)
            if (zdTicketOpt.nonEmpty) {
              val zdTicket = zdTicketOpt.get
              zdTicket.setCustomFields(customFieldList);
              if (comment.nonEmpty) {
                val commentObj = new Comment()
                commentObj.setPublic(false)
                commentObj.setBody(comment.get)
                zdTicket.setComment(commentObj)
              }
              val fromStatus = zdTicket.getStatus.toString
              val newStatus = Status.SOLVED
              zdTicket.setStatus(newStatus)
              val tkt: Ticket = zendesk.updateTicket(zdTicket)
              logger.debug("Ticket Id [" + tkt.getId + "] updated successfully @ [" + tkt.getUpdatedAt + "]")
              if (tkt.getStatus == newStatus) {
                val ticketUpdatedInDb = zendeskRepository.updateTicketStatus(tktId, fromStatus, newStatus.toString).map(isDone => {
                  isDone
                }) recover {
                  case e: Exception => {
                    logger.error("{}", e)
                    false
                  }
                }
              }
            }
          })
          true
        } else {
          false
        }
      }) recover {
        case e: Exception => {
          logger.error("{}", e)
          false
        }
      }
    } else {
      logger.debug("Ticket posting not allowed")
      Future.apply(false)
    }
  }

  def updateTicketsOnBseRegistration(userid: Long, purpose: Option[Long], comment: Option[String]): Future[Boolean] = {
    //val purpose = Some(zendeskHelper.TKT_PURPOSE_KYC)
    if (zendeskHelper.isTktAllowed) {
      zendeskRepository.getTicketsByUserIdANDpurpose(userid, purpose).map(fctktRows => {
        val retvalList = ListBuffer[Long]()
        if (fctktRows.nonEmpty) {
          for (tktRow <- fctktRows) {
            if (!tktRow.tktticketid.equalsIgnoreCase("0"))
              retvalList.+=(tktRow.tktticketid.toLong)
          }
        }
        if (retvalList.length > 0) {
          retvalList.foreach(tktId => {
            logger.debug("fetching ticket id >> " + tktId)
            val zdTicketOpt = getTicket(tktId)
            if (zdTicketOpt.nonEmpty) {
              val zdTicket = zdTicketOpt.get
              if (comment.nonEmpty) {
                val commentObj = new Comment()
                commentObj.setPublic(false)
                commentObj.setBody(comment.get)
                zdTicket.setComment(commentObj)
              }
              val tkt: Ticket = zendesk.updateTicket(zdTicket)
              logger.debug("Ticket Id [" + tkt.getId + "] updated successfully @ [" + tkt.getUpdatedAt + "]")
            }
          })
          true
        } else {
          false
        }
      }).recover {
        case e: Exception => {
          logger.error("{}", e)
          false
        }
      }
    } else {
      logger.debug("Ticket posting not allowed")
      Future.apply(false)
    }
  }


  /**
    *
    * @param userid
    * @param username
    * @param kycStatName
    * @return
    */
  def updateZendeskTicketForKyc(userid: Long, username: String, kycStatName: String): Future[Boolean] = {
    updateTicketAndUserKycStatusByPurpose(userid, username, kycStatName, Some(zendeskHelper.TKT_PURPOSE_REGISTRATION), false).flatMap(res => {
      updateTicketAndUserKycStatusByPurpose(userid, username, kycStatName, Some(zendeskHelper.TKT_PURPOSE_DOCUMENT_UPLOADED), res._2).map(tktUpdRes => tktUpdRes._1)
        .recover { case e: Exception => {
          logger.error("{}", e)
          false
        }
        }
    }).recover {
      case e: Exception => {
        logger.error("{}", e)
        false
      }
    }
  }

  /**
    *
    * @param userid
    * @param username
    * @param kycStatName
    * @param purpose
    * @param isUserUpdated
    * @return
    */
  def updateTicketAndUserKycStatusByPurpose(userid: Long, username: String, kycStatName: String,
                                            purpose: Option[Long], isUserUpdated: Boolean): Future[(Boolean, Boolean)] = {
    if (zendeskHelper.isTktAllowed) {
      val customField: CustomFieldValue = new CustomFieldValue(zendeskHelper.ZENDESK_TICKET_KYC_STATUS, kycStatName)
      val customFieldList = new java.util.ArrayList[CustomFieldValue]
      customFieldList.add(customField)
      zendeskRepository.getTicketsByUserIdANDpurpose(userid, purpose).map(fctktRows => {
        if (fctktRows.nonEmpty) {
          for (tktRow <- fctktRows) yield {
            if (!tktRow.tktticketid.equalsIgnoreCase("0"))
              Some(tktRow.tktticketid.toLong)
            else
              None
          }
        } else {
          /*var oldTktrfnum: Option[Long] = None
          if (fctktRows.nonEmpty) {
            oldTktrfnum = Some(fctktRows.head.id)
          }
          val requester = zendeskHelper.createRequester
          requester.setEmail(username)
          createTicket(requester, zendeskHelper.onBoardingSubject, None, Some(zendeskHelper.ZENDESK_GROUP_ONBOARDING), username, username, userid, None, purpose, oldTktrfnum).map(tktIdOption => {
            List.apply(tktIdOption)
          })*/
          List[Option[Long]]();
        }
      }).map(tktList => {
        tktList.foreach(tktOption => {
          val tktId = tktOption.get
          logger.debug("updateTicketAndUserKycStatusByPurpose >>> zendesk ticket id >>>> " + tktId)
          setCustomField(tktId, customFieldList, None, None, None)
        })

        if (tktList.nonEmpty && !isUserUpdated) {
          val zdUserId = zendeskHelper.getUserIdByEmail(username)
          val userField = new java.util.HashMap[String, AnyRef]()
          userField.put(zendeskHelper.ZENDESK_USER_FIELD_NAME_KYC, kycStatName)
          setUserField(zdUserId, userField)
          (true, true)
        } else {
          (true, false)
        }
      }) recover {
        case e: Exception => {
          logger.error("{}", e)
          (false, false)
        }
      }
    } else {
      Future.apply(false, false);
    }
  }

  def updateKycFromZendeskRequest(kycRequest: JsObject): Future[(Boolean, String)] = {
    val userId = (kycRequest \ "userId").asOpt[JsValue]
    val userName = (kycRequest \ "userName").asOpt[JsValue]
    val kycStatus = (kycRequest \ "kyc").asOpt[JsValue]
    val pan = (kycRequest \ "pan").asOpt[JsValue]
    val mobile = (kycRequest \ "mobile").asOpt[JsValue]
    val tags = (kycRequest \ "tags").asOpt[JsValue]

    if (userId.nonEmpty && kycStatus.nonEmpty) {
      val userIdString = userId.get.as[String]
      val kycStatusSting = kycStatus.get.as[String]
      if (userIdString.trim.length > 0 && kycStatusSting.trim.length > 0) {
        var kyc = KYC_NOTDONE
        var kycType = ""
        if (kycStatusSting.trim == ZENDESK_TICKET_VALUE_AADHAAR_EKYC || kycStatusSting.trim == ZENDESK_TICKET_VALUE_KYC_DONE_EKYC) {
          kyc = KYC_EXTERNALLY_DONE
          kycType = KYCTYPE_AADHAR
        } else if (kycStatusSting.trim == ZENDESK_TICKET_VALUE_KYC_DONE || kycStatusSting.trim == ZENDESK_TICKET_VALUE_KYC_DONE_BY_FINCASH) {
          kyc = KYC_DONE
          kycType = KYCTYPE_PAN
        } else if (kycStatusSting.trim == ZENDESK_TICKET_VALUE_KYC_NOT_DONE || kycStatusSting.trim == ZENDESK_TICKET_VALUE_EKYC_FAILED) {
          kyc = KYC_NOTDONE
        } else if (kycStatusSting.trim == ZENDESK_TICKET_VALUE_KYC_UNDER_PROCESS) {
          kyc = KYC_UNDERPROCESS
        }

        kycRepository.updateuserKYCStatus(userIdString.trim.toLong, ZENDESK_SOURCE_API, kyc, kycType).map(isUpdated => {
          if (isUpdated) {
            // updating user details in zendesk in case of successful update in DB
            if (userName.nonEmpty) {
              val username = userName.get.as[String]
              val zdUserId = zendeskHelper.getUserIdByEmail(username)
              val userField = new java.util.HashMap[String, AnyRef]()
              userField.put(zendeskHelper.ZENDESK_USER_FIELD_NAME_KYC, kycStatusSting.trim)
              setUserField(zdUserId, userField)
            }
            (true, "Kyc Status updation Successful")
          }
          else {
            (false, "KYC status Updation not successful")
          }
        }) recover {
          case e: Exception => {
            logger.error("{}", e)
            (false, e.getMessage)
          }
        }
      }
      else {
        Future.apply((false, "UseId or KycStatus is Empty String"))
      }
    }
    else {
      Future.apply((false, "UseId or KycStatus is Empty"))
    }
  }

}
