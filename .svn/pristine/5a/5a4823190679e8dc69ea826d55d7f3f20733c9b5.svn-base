package repository.module

import com.google.inject.Inject
import constants.{CNDConstants, DBConstants, KycConstants}
import data.model.Tables.{Fcbua, Fcdmt, FcdmtRowWrapper, Fckyc, FckycRow, FckycRowWrapper, Fcuaa, FcuaaRowWrapper}
import org.slf4j.LoggerFactory
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repository.tables._
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * Created by Fincash on 28-01-2017.
  */
class KycRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, implicit val ec: ExecutionContext, fcuaaRepo: FcuaaRepo, fcamtRepo: FcamtRepo, fckycRepo: FckycRepo,
                              cndRepository: CNDRepository, fcbuaRepo: FcbuaRepo, fcdmtRepo: FcdmtRepo)
      extends HasDatabaseConfigProvider[JdbcProfile] with CNDConstants with KycConstants with DBConstants{
  val logger, log = LoggerFactory.getLogger(classOf[KycRepository])

  def getUserKYCStatus(userPk:Long):Future[Seq[FckycRow]] = {
    fckycRepo.filter(_.kycubdrfnum === userPk)
  }

  def updateuserKYCStatus(userid: Long, username: String, kycStatus: String, kycType: String): Future[Boolean] = {
    var fckycRowWrapper = new FckycRowWrapper(None, userid, None, None, None, None, None, None, kycStatus, None, Option(kycType), None, None, None, DOC_UNDERPROCESS, DOC_UNDERPROCESS, DOC_UNDERPROCESS)

    var query = for {
      kycrow <- Fckyc.filter(x => x.kycubdrfnum === userid)
    } yield (kycrow)
    db.run(query.result).flatMap(values => {
      if (values.isEmpty){
        // Insert in FCKYC
        var query2 = for {
          kycnewInstance <- Fckyc returning Fckyc.map(_.id) into ((kycObj, id) => kycObj.copy(id = id)) += fckycRowWrapper.get(username)
        } yield ()
        db.run(query2).map(values => {
          true
        }).recover {
          case ex: Exception =>
            logger.error(ex.getMessage + " Can't Insert UserKYCstatus")
            throw ex
        }
      }
      else {
        // Update in FCKYC
        var query2 = for {
            kycInstance <- Fckyc.filter(x => x.kycubdrfnum === userid).map(x => (x.kycstatus, x.kyctype)).update((kycStatus, Option(kycType)))
        } yield ()
        db.run(query2).map(values => {
          true
        }).recover {
          case ex: Exception =>
            logger.error(ex.getMessage + " Can't Update UserKYCstatus")
            throw ex
        }
      }
    }).recover {
      case ex: Exception =>
        logger.error(ex.getMessage + " Can't save User Fatca")
        throw ex
    }
  }

  def updatePhotoImageDmt(userid: Long, dmtid: Long): Future[String] = {
//    var fckycRowWrapper = new FckycRowWrapper(None, userid, Option(dmtid), None, None, None, None, None, KYC_NOTDONE, None, None);
//    fckycRepo.filter(_.kycubdrfnum === userid).copy(kycdmtphoto = dmtid)
    var query = Fckyc.filter(_.kycubdrfnum === userid).map(x => (x.kycdmtphoto)).update((Option(dmtid)))
    db.run(query).map(values => {
      dmtid.toString
    }).recover{
      case ex: Exception =>
        logger.error(ex.getMessage + "Can't Update FCKYC")
        throw ex
    }
  }

  def updateAddressProofDmt(userid: Long, dmtid: Long): Future[String] = {
    var fcuaaRowWrapper = new FcuaaRowWrapper(None, userid, 1, 1, 1, Option(dmtid), None, None, None, DOC_UNDERPROCESS)

    var query = for {
      uaaInstance <- Fcuaa.filter(x => (x.uaaubdrfnum===userid && x.uaacndcontactaddress === CND_CONTACTADDRESS_PERMANENT.toLong)).map(x => (x.uaadmtrfnum)).update((Option(dmtid)))
    } yield ()
    db.run(query).map(values => {
      dmtid.toString
    }).recover {
      case ex: Exception =>
        logger.error(ex.getMessage + "Can't Update FCUAA")
        throw ex
    }
  }

  def updateIdProofDmt(userid: Long, username: String, dmtid: Long): Future[String] = {
    var fckycRowWrapper = new FckycRowWrapper(None, userid, Option(dmtid), None, None, None, None, None, KYC_NOTDONE, None, None, None, None, None,
      DOC_UNDERPROCESS, DOC_UNDERPROCESS, DOC_UNDERPROCESS)
    fckycRepo.filter(x => x.kycubdrfnum === userid).flatMap(value => {
//      var row = value.headOption.get
      if (value.headOption.isEmpty){
        var query = for {
          kycNewInstance <- Fckyc returning Fckyc.map(_.id) into ((kycObj, id) => kycObj.copy(id = id)) += fckycRowWrapper.get(username)
        } yield (kycNewInstance)
        db.run(query).map(values => {
          values.id.toString
        }).recover {
          case ex: Exception =>
            logger.error(ex.getMessage + "Can't create KYC table row")
            throw ex
        }
      }
      else {
        var query = for {
          kycInstance <-  Fckyc.filter(x => (x.kycubdrfnum === userid)).map(x => (x.kycdmtrfnum)).update((Option(dmtid)))
        } yield ()
        db.run(query).map(values => {
          dmtid.toString
        }).recover {
          case ex: Exception =>
            logger.error(ex.getMessage + "Can't Update FCKYC")
            throw ex
        }
      }
    })
  }

  def updateBankProofDmt(userid: Long, dmtid: Long): Future[String] = {
    fcbuaRepo.filter(x => x.buaubdrfnum === userid).map(value => {
      if (!value.headOption.isEmpty){
        var query = for {
          buaNewInstance <- Fcbua.filter(x => (x.buaubdrfnum === userid)).map(x => (x.buadmtrfnum, x.buadocstatus)).update((Option(dmtid), UNAPPORVED_FLAG))
        } yield ()
        db.run(query).map(values => {

        }).recover {
          case ex: Exception =>
            logger.error(ex.getMessage + "Can't Update FCBUA")
            throw ex
        }
      }
      dmtid.toString
    })
  }

  def updateSignatureImageDmt(userid: Long, dmtid: Long): Future[String] = {
    //    var fckycRowWrapper = new FckycRowWrapper(None, userid, Option(dmtid), None, None, None, None, None, KYC_NOTDONE, None, None);
    //    fckycRepo.filter(_.kycubdrfnum === userid).copy(kycdmtphoto = dmtid)
    var query = Fckyc.filter(_.kycubdrfnum === userid).map(x => (x.kycdmtsignature)).update((Option(dmtid)))
    db.run(query).map(values => {
      dmtid.toString
    }).recover{
      case ex: Exception =>
        logger.error(ex.getMessage + "Can't Update FCKYC")
        throw ex
    }
  }

  def getDmtIds(userid: Long): Future[(String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String)] = {

    val retval = for {
      kycrow <- fckycRepo.filter(_.kycubdrfnum === userid)
      uaarow <- fcuaaRepo.filter(x => (x.uaaubdrfnum === userid && x.uaacndcontactaddress === CND_CONTACTADDRESS_PERMANENT.toLong))
      buarow <- fcbuaRepo.filter(x => (x.buaubdrfnum === userid))
    } yield (kycrow.headOption,uaarow.headOption, buarow.headOption)

    retval.map(values => {
      var photodmtid, addressdmtid, iddmtid, bankdmtid, signaturedmtid = Option("")
      var photopath, addresspath, idpath, bankpath, signaturepath = Option("")
      var photocnd, addresscnd, idcnd, bankcnd, signaturecnd = Option("")
      var photostatus, addressstatus, idstatus, bankstatus, signaturestatus = Option("")

      if (values._1.nonEmpty && values._1.get.kycdmtphoto.nonEmpty){
        photodmtid = values._1.get.kycdmtphoto.map(_.toString)
        if (photodmtid.nonEmpty){
          val a = fcdmtRepo.getById(photodmtid.get.toLong).map(x => {
            if (x.nonEmpty){
              photopath = x.get.dmtpath
              photocnd = Some(x.get.dmtcndtyperfnum.toString)
              val b = fckycRepo.filter(_.kycdmtphoto === photodmtid.get.toLong).map(y => {
                if (y.nonEmpty){
                  photostatus = Some(y.head.kycphotoverification)
                }
              })
              Await.result(b, Duration.Inf)
            }
          })
          Await.result(a, Duration.Inf)
        }
      }
      if (values._1.nonEmpty && values._1.get.kycdmtrfnum.nonEmpty  ){
        iddmtid = values._1.get.kycdmtrfnum.map(_.toString)
        if (iddmtid.nonEmpty){
          val a =fcdmtRepo.getById(iddmtid.get.toLong).map(x => {
            if (x.nonEmpty){
              idpath = x.get.dmtpath
              idcnd = Some(x.get.dmtcndtyperfnum.toString)
              val b = fckycRepo.filter(_.kycdmtrfnum === iddmtid.get.toLong).map(y => {
                if (y.nonEmpty){
                  idstatus = Some(y.head.kycidverification)
                }
              })
              Await.result(b, Duration.Inf)
            }
          })
          Await.result(a, Duration.Inf)
        }
      }
      if (values._2.nonEmpty && values._2.get.uaadmtrfnum.nonEmpty){
        addressdmtid = values._2.get.uaadmtrfnum.map(_.toString)
        if (addressdmtid.nonEmpty){
          val a = fcdmtRepo.getById(addressdmtid.get.toLong).map(x => {
            if (x.nonEmpty){
              addresspath = x.get.dmtpath
              addresscnd = Some(x.get.dmtcndtyperfnum.toString)
              val b = fcuaaRepo.filter(_.uaadmtrfnum === addressdmtid.get.toLong).map(y => {
                if (y.nonEmpty){
                  addressstatus = Some(y.head.uaadocstatus)
                }
              })
              Await.result(b, Duration.Inf)
            }
          })
          Await.result(a, Duration.Inf)
        }
      }
      if (values._3.nonEmpty && values._3.get.buadmtrfnum.nonEmpty){
        bankdmtid = values._3.get.buadmtrfnum.map(_.toString)
        if (bankdmtid.nonEmpty){
          val a = fcdmtRepo.getById(bankdmtid.get.toLong).map(x => {
            if (x.nonEmpty){
              bankpath = x.get.dmtpath
              bankcnd = Some(x.get.dmtcndtyperfnum.toString)
              val b = fcbuaRepo.filter(_.buadmtrfnum === bankdmtid.get.toLong).map(y => {
                if (y.nonEmpty){
                  bankstatus = Some(y.head.buadocstatus)
                }
              })
              Await.result(b, Duration.Inf)
            }
          })
          Await.result(a, Duration.Inf)
        }
      }
      if (values._1.nonEmpty && values._1.get.kycdmtsignature.nonEmpty){
        signaturedmtid = values._1.get.kycdmtsignature.map(_.toString)
        if (signaturedmtid.nonEmpty){
          val a = fcdmtRepo.getById(signaturedmtid.get.toLong).map(x => {
            if (x.nonEmpty){
              signaturepath = x.get.dmtpath
              signaturecnd = Some(x.get.dmtcndtyperfnum.toString)
              val b = fckycRepo.filter(_.kycdmtsignature === signaturedmtid.get.toLong).map(y => {
                if (y.nonEmpty){
                  signaturestatus = Some(y.head.kycsignverification)
                }
              })
              Await.result(b, Duration.Inf)
            }
          })
          Await.result(a, Duration.Inf)
        }
      }
      (photodmtid.get, iddmtid.get, addressdmtid.get, bankdmtid.get, signaturedmtid.get,
       photocnd.get, idcnd.get, addresscnd.get, bankcnd.get, signaturecnd.get,
       photopath.get, idpath.get, addresspath.get, bankpath.get, signaturepath.get,
       photostatus.get, idstatus.get, addressstatus.get, bankstatus.get, signaturestatus.get)
    })
  }

  def updateDocumentPath(userid: Long, username: String, dmtid: String, path: String, doctypecnd: Long): Future[(String, String, String)] = {
    var fcdmtRowWrapper = new FcdmtRowWrapper(None, doctypecnd, Option(path), None, None, None, None)

    if (dmtid.isEmpty){
      // Insert in FCDMT
      var query2 = for {
        dmtnewInstance <- Fcdmt returning Fcdmt.map(_.id) into ((dmtObj, id) => dmtObj.copy(id = id)) += fcdmtRowWrapper.get(username)
      } yield (dmtnewInstance)
      db.run(query2).flatMap(values => {
        cndRepository.getCnd(doctypecnd).map(cndRow => {
          if(cndRow.nonEmpty){
            val row = cndRow.get
            val a = row.cndgroup match {
              case ADDRESS_PROOF => updateAddressProofDmt(userid, values.id)
              case ID_PROOF => updateIdProofDmt(userid, username, values.id)
              case BANK_PROOF => updateBankProofDmt(userid, values.id)
              case PHOTO_PROOF => updatePhotoImageDmt(userid, values.id)
              case SIGNATURE_PROOF => updateSignatureImageDmt(userid, values.id)
            }
            (values.id.toString, values.dmtcndtyperfnum.toString, values.dmtpath.get)
          }else{
            logger.debug("No cnd Data found in database for id " + doctypecnd)
            // no cnd found case
            ("","","")
          }
        })
      }).recover {
        case ex: Exception =>
          logger.error(ex.getMessage + " Can't Insert UserKYCstatus")
          throw ex
      }
    }
    else {
      // Update in FCKMT
      var query2 = for {
        dmtInstance <- Fcdmt.filter(x => x.id === dmtid.toLong).map(x => (x.dmtcndtyperfnum, x.dmtpath)).update((doctypecnd, Option(path)))
      } yield ()
      db.run(query2).map(values => {
        (dmtid, doctypecnd.toString, path)
      }).recover {
        case ex: Exception =>
          logger.error(ex.getMessage + " Can't Update UserKYCstatus")
          throw ex
      }
    }
  }
}
