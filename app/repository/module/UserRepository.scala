package repository.module

import java.sql.Timestamp
import java.util.Date
import javax.inject.Singleton

import com.google.inject.Inject
import constants._
import data.model.Tables.{Fcamt, FceubdRow, FcamtRowWrapper, Fcbbt, FcbbtRowWrapper, Fcbmt, FcbmtRow, FcbmtRowWrapper, Fcbua, FcbuaRowWrapper, Fccnd, Fcefut, FcefutRowWrapper, Fceubd, FceubdRowWrapper, Fcfut, FcfutRowWrapper, Fckcl, FckclRowWrapper, Fckyc, FckycRowWrapper, Fcndt, FcndtRowWrapper, Fcoaf, FcoafRow, FcoafRowWrapper, Fcotpt, FcotptRow, FcotptRowWrapper, Fctkt, FctktRow, FctktRowWrapper, Fctkth, FctkthRowWrapper, Fcuaa, FcuaaRowWrapper, FcuactRowWrapper, Fcubd, FcubdRow, FcubdRowWrapper, Fcull, FcullRowWrapper, Fcult, FcultRow, FcultRowWrapper, Fcuoa, FcuoaRowWrapper, Fcupd, FcupdRow, FcupdRowWrapper}
import models._
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repository.tables._
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import utils.DateTimeUtils

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class UserRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, implicit val ec: ExecutionContext, fcubdRepo: FcubdRepo, fcultRepo: FcultRepo,
                               fcuftRepo: FcuftRepo, fcbmtRepo: FcbmtRepo, fcbbtRepo: FcbbtRepo, fcbuaRepo: FcbuaRepo, fcotptRepo: FcotptRepo,
                               fcupdRepo: FcupdRepo, fcfutRepo: FcfutRepo, fcndtRepo: FcndtRepo, fcamtRepo: FcamtRepo, fcoafRepo: FcoafRepo, fcuaaRepo: FcuaaRepo, fcuactRepo: FcuactRepo,
                               fcefutRepo: FcefutRepo, fceubdRepo: FceubdRepo, fcullRepo: FcullRepo, fctktRepo: FctktRepo, fctkthRepo: FctkthRepo)
  extends IntegrationConstants with BaseConstants with KycConstants with HTTPErrorConstants with DBConstants with DateConstants with CNDConstants with HasDatabaseConfigProvider[JdbcProfile] {


  val logger, log = LoggerFactory.getLogger(classOf[UserRepository])


  /********************GET PK & USER ROW APIs****************************/
  def getUserByPk(userPk: Long): Future[Option[FcubdRow]] = {
    fcubdRepo.getById(userPk)
  }

  def getUBDandEUBDbyUserid(userid: Long): Future[Seq[(FcubdRow, FceubdRow)]] = {
    val query = for {
      fcubdrow <- Fcubd.filter(_.id === userid)
      fceubdrow <- Fceubd.filter(_.id === userid)
    } yield {(fcubdrow, fceubdrow)}

    db.run(query.result).map(_data => {
      _data
    })
  }

  def addErrorPanToKycLog(pan: String, username: String): Future[Long] = {
    val kclObj = new FckclRowWrapper(id = None, kclpanno = pan, kclnoofattempts = 0).get(username)
    val query = for {
      kclObjList <- Fckcl.filter(x => x.kclpanno === pan).result
      kclLogObj <- kclObjList.size match {
        case 0 => {Fckcl returning Fckcl.map(_.id) into ((kcl, id) => kcl.copy(id = id)) += kclObj};
        case n => {DBIO.successful(0)};
      }
    } yield(kclLogObj)

    db.run(query).map(_ => 1L);
  }

  def getAllUsersByPan(pan: String): Future[Seq[FcubdRow]] = {
    fcubdRepo.filter(_.ubdpan === pan.toUpperCase).map(rows => {
      rows
    })
  }

  def getVarfiedUsersByPan(pan: String): Future[Seq[FcubdRow]] = {
    val query = for {
      ubdrow <- Fcubd.filter(_.ubdpan === pan.toUpperCase)
      eubdrow <- Fceubd.filter(_.eubdisvarified === Y_FLAG)
    } yield (ubdrow)

    db.run(query.result).map(_list => {
      _list
    })
  }

  def getNonVarfiedUsersByPan(pan: String): Future[Seq[FcubdRow]] = {
    val query = for {
      ubdrow <- Fcubd.filter(_.ubdpan === pan.toUpperCase)
      eubdrow <- Fceubd.filter(_.eubdisvarified === N_FLAG)
    } yield (ubdrow)

    db.run(query.result).map(_list => {
      _list
    })
  }

  def getUserByUsername(username: String): Future[Option[FcubdRow]] = {
    if(username.isEmpty){
      return Future.apply(None)
    }
    fcultRepo.filter(x => x.ultusername === username.toLowerCase).flatMap(row => {
        if(row.nonEmpty){
            val userid = row.head.ultubdrfnum
            fcubdRepo.getById(userid).map(userRow =>{
              userRow
            })
        }else{
          Future.apply(None)
        }
    })
    /*fcubdRepo.filter(x => x.ubdemailid === username).map(y => y.headOption)*/
  }

  def getFirstNameByUsername(username: String): Future[String] = {
    fcubdRepo.filter(x => x.ubdemailid === username).map(y =>
      if (y.headOption.nonEmpty){
        y.last.ubdfirstname.getOrElse("")
      }
      else {
        ""
        //throw new Exception("No user found with " + username)
      }
    )
  }

  def getUsernameByUserid(userid: Long): Future[String] = {
    fcultRepo.filter(x => x.ultubdrfnum === userid).map(y => y.head.ultusername)
  }

  def getUserIdByUsername(username: String): Future[Long] = {
    fcultRepo.filter(x => x.ultusername === username).map(y => {
      if (!y.headOption.isEmpty){
        y.headOption.head.ultubdrfnum
      }
      else {
        throw new Exception("No user found with " + username)
      }
    })
  }

  /*******************************SAVE PAN & MOB API***********************/
  def savePanNumber(userid: Long, pan: String) : Future[Boolean] = {
    this.getUserByPk(userid).flatMap(user => {
      if (!user.isEmpty) {
        var userRow = user.get.copy(ubdpan = Some(pan.toUpperCase), modifydate = DateTimeUtils.getCurrentTimeStamp)
        fcubdRepo.updateById(userid, userRow).map(value => {
          true
        })
      }
      else {
        Future.apply(false)
      }
    })
  }

  def checkPanExists(userid: Long, pan: String): Future[Boolean] = {
    val query = for {
      ubdObjList <- Fcubd.filter(x => x.ubdpan === pan && x.id =!= userid).result
      eubdObjList <- {
        DBIO.sequence(for (ubd <- ubdObjList) yield {
          Fceubd.filter(x => x.eubdisvarified === Y_FLAG && x.id === ubd.id).result
        })
      }
    } yield(eubdObjList.flatten)

    db.run(query).map(dataList => {
      logger.debug(dataList.length + "");
      dataList.length > 0
    })
  }

  def saveMobNumber(userid: Long, mob: String): Future[Boolean] = {
    this.getUserByPk(userid).flatMap(user => {
      if (!user.isEmpty) {
        var userRow = user.get.copy(ubdmobileno = Some(mob), modifydate = DateTimeUtils.getCurrentTimeStamp)
        val query = Fcubd.insertOrUpdate(userRow)
        db.run(query)
          .map(values => {
            (values > 0)
          })
          .recover {
            case ex => logger.debug("save failed. Check stacktrace for more details.")
              ex.printStackTrace();
              throw new Exception("Save operation failed")
          }
      }
      else {
        Future.apply(false);
      }
    })
  }

  def updatePassword(key: String, password: String): Future[Boolean] = {
    fcuactRepo.filter(x => x.uactcode === key).flatMap(results => {
      if (results.nonEmpty) {
        var uact = results.head
        val userid = uact.uactubdrfnum
        fcultRepo.filter(y => y.ultubdrfnum === userid).flatMap(ults => {
          if (ults.nonEmpty) {
            var ult = ults.head
            ult = ult.copy(ultpassword = password)
            fcultRepo.updateById(ult.id, ult).flatMap(retval => {
              uact = uact.copy(uactisused = Y_FLAG)
              fcuactRepo.updateById(uact.id, uact).map(_retval => {
                true
              })
            })
          } else {
            Future.apply(false)
          }
        })
      } else {
        Future.apply(false)
      }
    })
  }

  /***********************MAIL LINK API********************************/

  def saveMailLink(_userid: Long, _uactcode: String, _days : Int, _uactisused: String, _username : String,ip:String): Future[Long] = {
    val fcuactRow = new FcuactRowWrapper(None,_uactcode, Some(DateTimeUtils.getTimestampAfterDays(_days)),_userid, _uactisused, Some(ip)).get(_username)
    fcuactRepo.save(fcuactRow).map(row => {
      row._2
    })
  }

  def verifyActivationCode(_actcode: String) : Future[Option[(Long,Long)]] = {
    val cdate = new Timestamp(new Date().getTime)
    //DateTimeUtils.convertDateToFormat(new Date(), "yyyy-MM-dd HH:mm:ss")
    fcuactRepo.filter(x => (x.uactcode === _actcode) && (x.uactisused =!= Y_FLAG) && (x.uactexpirydate >= cdate)).map( rows => {
      if(rows.nonEmpty){
        val head = rows.head
        Some((head.uactubdrfnum, head.id))
      }else{
        None
      }
    })
  }


  /***********************LOGIN SIGNUP API********************************/
  def signUpUser(username: String, password: String, ipAddress: String): Future[Long] = {
    //    val encryPass = BCrypt.hashpw("password", BCrypt.gensalt(12))
    var ubdrfnum: Long = 0
    var _username = username.toLowerCase

    var fcubdRowWrapper = new FcubdRowWrapper(None, None, None, None, None, None, _username, None, None, None,
      None, None, None, None, None, None,Some(TAX_STATUS_INDIVIDUAL),None)
    var fcultRowWrapper = new FcultRowWrapper(None, ubdrfnum, _username, password, None, "A", None, None).get(_username)
    val fcullRowWrapper = new FcullRowWrapper(None, 1, Some(DateTimeUtils.getCurrentTimeStamp), ipAddress, None, None, Y_FLAG)
    var fceubdRowWrapper = new FceubdRowWrapper(1, None, N_FLAG, None, N_FLAG, Some(N_FLAG), N_FLAG)
    var fckycRowWrapper = new FckycRowWrapper(None, 1, None, None, None, None, None, None, KYC_NOTDONE, None, None, None, None, None, DOC_UNDERPROCESS, DOC_UNDERPROCESS, DOC_UNDERPROCESS)
    var fcfutRowWrapper = new FcfutRowWrapper(None, 1, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None)
    var fcefutRowWrapper = new FcefutRowWrapper(1, Option(N_FLAG), None)

    fcultRepo.filter(x => x.ultusername === _username).flatMap(user => {
        if (user.isEmpty) {
        val query = for {
          ubdNewInstance <- Fcubd returning Fcubd.map(_.id) into ((ubdObj, id) => ubdObj.copy(id = id)) += (fcubdRowWrapper.get(username))
          ultNewInstance <- Fcult returning Fcult.map(_.id) into ((ultObj, id) => ultObj.copy(id = id)) += (fcultRowWrapper.copy(ultubdrfnum = ubdNewInstance.id))
          ullNewInstanef <- Fcull returning Fcull.map(_.id) into ((ullObj, id) => ullObj.copy(id = id)) += (fcullRowWrapper.get(username).copy(ullultrfnum = ultNewInstance.id))
          eubdNewInstance <- Fceubd += (fceubdRowWrapper.get(username).copy(id = ubdNewInstance.id))
          kycNewInstance <- Fckyc += (fckycRowWrapper.get(username).copy(kycubdrfnum = ubdNewInstance.id))
          futNewInstance <- Fcfut returning Fcfut.map(_.id) into ((futObj, id) => futObj.copy(id = id)) += (fcfutRowWrapper.get(username).copy(futubdrfnum = ubdNewInstance.id))
          efutNewInstance <- Fcefut += (fcefutRowWrapper.get(username).copy(id = futNewInstance.id))
        } yield (ubdNewInstance)

        db.run(query.transactionally).map(value => {
          value.id
        }).recover {
          case ex => logger.debug("save failed. Check stacktrace for more details.")
            ex.printStackTrace();
            throw new Exception("Save operation failed")
        }
      }
      else {
        Future.apply(ubdrfnum)
      }
    })
  }

  def checkUser(username: String): Future[Boolean] = {
    var _username = username.toLowerCase

    fcultRepo.filter(x => x.ultusername === _username).map(y => {
      if (y.isEmpty) {
        false
      }
      else {
        true
      }
    })
  }

  def signInUser(username: String, password: String, ipAddress: String): Future[(Long, Boolean, Boolean)] = {
    //var id: Long = 0
    val _username = username.toLowerCase

    fcultRepo.filter(x => x.ultusername === _username).flatMap(fcultRows => {
      if (fcultRows.isEmpty) {
        Future.apply((0L, false, false))
      }
      else {
        val fcullRowWrapper = new FcullRowWrapper(None, fcultRows.head.id,Some(DateTimeUtils.getCurrentTimeStamp), ipAddress, None, None, N_FLAG)
        if (BCrypt.checkpw(password, fcultRows.head.ultpassword)) {
          fcullRepo.saveWithKey(fcullRowWrapper.get(username).copy(ullissuccessful = Y_FLAG)).map(value => {
            (fcultRows.head.ultubdrfnum, true, true)
          })
        } else {
          fcullRepo.saveWithKey(fcullRowWrapper.get(username)).map(value => {
            (fcultRows.head.ultubdrfnum, true, false)
          })
        }
      }
    })
  }

  /********************GET USER DETAILS*****************************/
  def getUserBasic(userid: Long): Future[UserBasic] = {
    var firstName, middleName, lastName, panName, pan, mob, email, tel, fax, gender, fatherName, motherName = Option("")
    var dob: Option[Date] = None
    var dobString: Option[String] = None
    var maritalStatus: Option[Long] = None //Option(CND_MARITALSTATUS_MARRIED.toLong)

    for {
      userrow <- fcubdRepo.filter(x => x.id === userid)
      updrow <- fcupdRepo.filter(x => x.updubdrfnum === userid)
    } yield {
      if(userrow.headOption.head.ubdfirstname.isDefined)
        firstName = userrow.headOption.head.ubdfirstname
      if(userrow.headOption.head.ubdmiddlename.isDefined)
        middleName = userrow.headOption.head.ubdmiddlename
      if(userrow.headOption.head.ubdlastname.isDefined)
        lastName = userrow.headOption.head.ubdlastname

      if(userrow.headOption.head.ubdpanname.isDefined)
        panName = userrow.headOption.head.ubdpanname
      if(userrow.headOption.head.ubdpan.isDefined)
        pan = userrow.headOption.head.ubdpan
      if(userrow.headOption.head.ubdmobileno.isDefined)
        mob = userrow.headOption.head.ubdmobileno
      email = Option(userrow.headOption.head.ubdemailid)
      if(userrow.headOption.head.ubdteleno.isDefined)
        tel = userrow.headOption.head.ubdteleno
      if(userrow.headOption.head.ubdfaxno.isDefined)
        fax = userrow.headOption.head.ubdfaxno
      if(userrow.headOption.head.ubddob.isDefined){
        dob = userrow.headOption.head.ubddob
        dobString = Some(DateTimeUtils.convertDateToFormat(dob.get,YYYYMMDD));
      }

      if(userrow.headOption.head.ubdgender.isDefined)
        gender = userrow.headOption.head.ubdgender
      if(userrow.headOption.head.ubdcndmaritalstatusrfnum.isDefined)
        maritalStatus = userrow.headOption.head.ubdcndmaritalstatusrfnum
      if (!updrow.isEmpty) {
        fatherName = updrow.headOption.head.updfathername
        motherName = updrow.headOption.head.updmothername
      }

      var contact = new Contact(email, mob, tel, fax)
      var userBasic = new UserBasic(firstName, middleName, lastName, panName, pan, fatherName, motherName, Option(contact),
        dobString, gender, maritalStatus.map(_.toString))

      userBasic
    }
  }

  def getUserParentDetails(userid: Long): Future[FcupdRow] = {
    fcupdRepo.filter(x => x.updubdrfnum === userid).map{
      x => x.head
    }
  }

    def getUserFatca(userid: Long): Future[UserFatca] = {
    var income: Option[Long] = None//Option(CND_INCOME_LESSTHANFIVE.toLong)
    var occupation: Option[Long] = None//Option(CND_OCCUPATION_SERVICE.toLong)
    var sourceOfWealth: Option[Long] = None//Option(CND_WEALTHSOURCE_SALARY.toLong)
    var nationality: Option[Long] = Option(CND_COUNTRY_INDIA.toLong)
    var birthCountry: Option[Long] = Option(CND_COUNTRY_INDIA.toLong)
    var isIndianTaxPayer = Option(Y_FLAG)
    var taxCountry: Option[Long] = Option(CND_COUNTRY_INDIA.toLong)
    var futid, birthCity, politicallyExposed, politicallyRelated, tin = Option("")

    var userFatca = new UserFatca(futid, income.map(_.toString), occupation.map(_.toString), sourceOfWealth.map(_.toString), nationality.map(_.toString), birthCountry.map(_.toString), birthCity, isIndianTaxPayer, taxCountry.map(_.toString), tin, politicallyExposed, politicallyRelated)

    val query = for {
      ubdrow <- Fcubd.filter(x => x.id === userid)
      futrow <- Fcfut.filter(x => x.futubdrfnum === userid)
      efutrow <- Fcefut.filter(x => x.id === futrow.id)
    } yield (ubdrow, futrow, efutrow)
    db.run(query.result).map(values => {
      if (!values.isEmpty){
        if (values.head._1.ubdcndoccupationrfnum.isDefined)
          occupation = values.head._1.ubdcndoccupationrfnum
        if (values.head._1.ubdcndnationalityrfnum.isDefined)
          nationality = values.head._1.ubdcndnationalityrfnum

        futid = Option(values.head._2.id.toString)
        if (values.head._2.futcndincomeslabrfnum.isDefined)
          income = values.head._2.futcndincomeslabrfnum
        if (values.head._2.futcndbirthcountryrfnum.isDefined)
          birthCountry = values.head._2.futcndbirthcountryrfnum
        if (values.head._2.futbirthcity.isDefined)
          birthCity = values.head._2.futbirthcity
        if (values.head._2.futcndtaxresidentrfnum.isDefined)
          taxCountry = values.head._2.futcndtaxresidentrfnum
        if (values.head._2.futtaxpayeridno.isDefined)
          tin = values.head._2.futtaxpayeridno
        if (values.head._2.futpoliticallyexposed.isDefined)
          politicallyExposed = values.head._2.futpoliticallyexposed
        if (values.head._2.futpoliticallyrelated.isDefined)
          politicallyRelated = values.head._2.futpoliticallyrelated

        if (values.head._3.efutcndwealthsource.isDefined){
          sourceOfWealth = values.head._3.efutcndwealthsource
      }
      }
      if(taxCountry == Option(CND_COUNTRY_INDIA.toLong)){
        isIndianTaxPayer = Option(Y_FLAG)
      }
      else {
        isIndianTaxPayer = Option(N_FLAG)
      }
      userFatca = new UserFatca(futid, income.map(_.toString), occupation.map(_.toString), sourceOfWealth.map(_.toString), nationality.map(_.toString), birthCountry.map(_.toString), birthCity, isIndianTaxPayer, taxCountry.map(_.toString), tin, politicallyExposed, politicallyRelated)
      userFatca
    })
  }

  def getUserBank(userid: Long): Future[UserBank] = {
    var holderName: String = ""
    var accountType = CND_ACCTYPE_SAVINGS
    var accountNumber, buaid: String = ""
    var bankName: String = ""
    var IFSC: String = ""
    var bankType:Option[String] = None
    var bmtrfnum:Option[Long] = None
    var city, state, branch, district, MICR, address = Option("")

    val query = for{
      buarow <- Fcbua.filter(_.buaubdrfnum === userid)
      bbtrow <- Fcbbt if bbtrow.id === buarow.buabbtrfnum
      bmtrow <- Fcbmt if bmtrow.id === bbtrow.bbtbmtrfnum
    } yield(buarow.buaholdername, buarow.buacndaccttyperfnum, buarow.buaacctno, bbtrow.bbtifsccode, bbtrow.bbtstate, bbtrow.bbtbranchname,
            bbtrow.bbtdistrict, bbtrow.bbtmicr, bbtrow.bbtaddress, bmtrow.bmtbankname, buarow.id, bmtrow.bmtbanktype, bmtrow.id)

    db.run(query.result).map(values =>{
      if (!values.isEmpty){
        holderName = values.head._1
        accountType = values.head._2.toString
        accountNumber = values.head._3
        IFSC = values.head._4
        state = values.head._5
        branch = values.head._6
        district = values.head._7
        MICR = values.head._8
        address = values.head._9
        bankName = values.head._10
        buaid = values.head._11.toString
        bankType = values.head._12
        bmtrfnum = Some(values.head._13)
      }
      var bank = new Bank(Some(bankName), Some(IFSC), city, state, branch, district, MICR, address, bankType,bmtrfnum)
      var userBank = new UserBank(Some(buaid), Some(holderName), Some(accountType), Some(bank), Option(new Account("", "")), Some(accountNumber))

      userBank
    })
  }

  def getBankByMmmtbuarfum(mmtbuarfnum: Long): Future[UserBank] = {
    var holderName: String = ""
    var accountType = CND_ACCTYPE_SAVINGS
    var accountNumber, buaid: String = ""
    var bankName: String = ""
    var IFSC: String = ""
    var bankType:Option[String] = None
    var bmtrfnum:Option[Long] = None
    var city, state, branch, district, MICR, address = Option("")

    val query = for{
      buarow <- Fcbua.filter(_.id === mmtbuarfnum)
      bbtrow <- Fcbbt if bbtrow.id === buarow.buabbtrfnum
      bmtrow <- Fcbmt if bmtrow.id === bbtrow.bbtbmtrfnum
    } yield(buarow.buaholdername, buarow.buacndaccttyperfnum, buarow.buaacctno, bbtrow.bbtifsccode, bbtrow.bbtstate, bbtrow.bbtbranchname,
            bbtrow.bbtdistrict, bbtrow.bbtmicr, bbtrow.bbtaddress, bmtrow.bmtbankname, buarow.id, bmtrow.bmtbanktype, bmtrow.id)

    db.run(query.result).map(values =>{
      if (!values.isEmpty){
        holderName = values.head._1
        accountType = values.head._2.toString
        accountNumber = values.head._3
        IFSC = values.head._4
        state = values.head._5
        branch = values.head._6
        district = values.head._7
        MICR = values.head._8
        address = values.head._9
        bankName = values.head._10
        buaid = values.head._11.toString
        bankType = values.head._12
        bmtrfnum = Some(values.head._13)
      }
      var bank = new Bank(Some(bankName), Some(IFSC), city, state, branch, district, MICR, address, bankType,bmtrfnum)
      var userBank = new UserBank(Some(buaid), Some(holderName), Some(accountType), Some(bank), Option(new Account("", "")), Some(accountNumber))

      userBank
    })
  }

  def getUserAddress(userid: Long): Future[UserAddress] = {
    var amtidC, amtidP, addressTypeC, addressTypeP, countryC, countryP, districtC, districtP, pinC, pinP = Option("")
    var address1P, address1C, address2C, address2P, landmarkC, landmarkP, stateC, stateP, cityC, cityP = Option("")

    var addressC = new Address(amtidC, addressTypeC, address1C, address2C, pinC.map(_.toString), cityC, districtC, stateC, countryC, landmarkC)
    var addressP = new Address(amtidP, addressTypeP, address1P, address2P, pinP.map(_.toString), cityP, districtP, stateP, countryP, landmarkP)
    var userAddress = new UserAddress(Some(addressP), Some(addressC), true)

    val query = for{
      uaarowPermanent <- Fcuaa.filter(x => x.uaaubdrfnum === userid && x.uaacndcontactaddress === CND_CONTACTADDRESS_PERMANENT.toLong)
      amtrowPermanent <- Fcamt.filter(x => x.id === uaarowPermanent.uaaamtrfnum)
      oafrowPermanent <- Fcoaf.filter(x => x.oafamtrfnum === amtrowPermanent.id)
    } yield(uaarowPermanent,amtrowPermanent,oafrowPermanent)

    db.run(query.result).flatMap(values =>{
      if (!values.isEmpty){
        amtidP = Option(values.head._2.id.toString)
        addressTypeP = Option(values.head._2.amtaddtype)
        address1P = Option(values.head._2.amtaddline1)
        address2P = values.head._2.amtaddline2
        pinP = values.head._3.oafpin
        cityP = values.head._3.oafcity
        if (values.head._3.oafdistrict.isDefined)
          districtP = values.head._3.oafdistrict
        if (values.head._3.oafstate.isDefined)
          stateP = values.head._3.oafstate
        if (values.head._2.amtcndstaterfnum.isDefined)
          stateP = values.head._2.amtcndstaterfnum.map(_.toString)
        if (values.head._3.oafcountry.isDefined)
          countryP = values.head._3.oafcountry
        if (values.head._2.amtcndcountryrfnum.isDefined)
          countryP = values.head._2.amtcndcountryrfnum.map(_.toString)
        landmarkP = values.head._2.amtaddlandmark
      }
      val query2 = for {
        uaarowCorrespondance <- Fcuaa.filter(x => x.uaaubdrfnum === userid && x.uaacndcontactaddress === CND_CONTACTADDRESS_CORRESPONDENCE.toLong)
        amtrowCorrespondance <- Fcamt.filter(x => x.id === uaarowCorrespondance.uaaamtrfnum)
        oafrowCorrespondance <- Fcoaf.filter(x => x.oafamtrfnum === amtrowCorrespondance.id)
      } yield(uaarowCorrespondance, amtrowCorrespondance, oafrowCorrespondance)
      db.run(query2.result).map(values2 => {
        if (!values2.isEmpty){
          amtidC = Option(values2.head._2.id.toString)
          addressTypeC = Option(values2.head._2.amtaddtype)
          address1C = Option(values2.head._2.amtaddline1)
          address2C = values2.head._2.amtaddline2
          pinC = values2.head._3.oafpin
          cityC = values2.head._3.oafcity
          if (values2.head._3.oafdistrict.isDefined)
            districtC = values2.head._3.oafdistrict
          if (values2.head._3.oafstate.isDefined)
            stateC = values2.head._3.oafstate
          if (values2.head._2.amtcndstaterfnum.isDefined)
            stateC = values2.head._2.amtcndstaterfnum.map(_.toString)
          if (values2.head._3.oafcountry.isDefined)
            countryC = values2.head._3.oafcountry
          if (values2.head._2.amtcndcountryrfnum.isDefined)
            countryC = values2.head._2.amtcndcountryrfnum.map(_.toString)
          landmarkC = values2.head._2.amtaddlandmark

          addressC = new Address(amtidC, addressTypeC, address1C, address2C, pinC.map(_.toString), cityC, districtC, stateC, countryC, landmarkC)
          addressP = new Address(amtidP, addressTypeP, address1P, address2P, pinP.map(_.toString), cityP, districtP, stateP, countryP, landmarkP)
          userAddress = new UserAddress(Some(addressP), Some(addressC), amtidP==amtidC)

          userAddress
        }
        else {
          userAddress
        }
      })
    })
  }

  def getUserNominee(userid: Long): Future[Nominee] = {
    var nomineeRelation: Option[Long] = None
    var nomineeDob: Option[Date] = None
    var dobString: Option[String] = None
    var amtid, ndtid, nomineeName = Option("")
    var addressType = Option("")
    var address1, address2, landmark, state, city, pin, district, country = Option("")

    var address = new Address(amtid, addressType, address1, address2, pin, city, district, state, country, landmark)
    var userNominee = new Nominee(ndtid, nomineeName, nomineeRelation.map(_.toString), nomineeDob.map(_.toString), Some(address))

    fcndtRepo.filter(x => x.ndtubdrfnum === userid).flatMap(ndtrow => {
      if (!ndtrow.isEmpty){
        val head = ndtrow.head
        ndtid = Option(head.id.toString)
        nomineeName = head.ndtnomineename
        nomineeRelation = head.ndtcndrelationshiprfnum
        if(head.ndtdob.isDefined){
          nomineeDob = head.ndtdob
          dobString = Some(DateTimeUtils.convertDateToFormat(nomineeDob.get,YYYYMMDD))
          userNominee = userNominee.copy(nomineeDob = dobString)
        }
        nomineeDob = head.ndtdob
        fcamtRepo.filter(x => x.id === ndtrow.head.ndtamtaddressrfnum).flatMap(amtrow => {
          if (!amtrow.isEmpty){
            val amthead = amtrow.head
            amtid = Option(amthead.id.toString)
            addressType = Option(amthead.amtaddtype)
            address1 = Option(amthead.amtaddline1)
            if (amtrow.headOption.get.amtaddline2.isDefined)
              address2 = amthead.amtaddline2
            if (amthead.amtcndstaterfnum.isDefined)
              state = amthead.amtcndstaterfnum.map(_.toString)
            if (amthead.amtcndcountryrfnum.isDefined)
              country = amthead.amtcndcountryrfnum.map(_.toString)
            if (amtrow.headOption.get.amtaddlandmark.isDefined)
              landmark = amthead.amtaddlandmark
            fcoafRepo.filter(x => x.oafamtrfnum === amtrow.head.id).map(oafrow => {
              if (!oafrow.isEmpty) {
                pin = oafrow.headOption.head.oafpin
                city = oafrow.headOption.head.oafcity
                if (oafrow.headOption.head.oafdistrict.isDefined)
                  district = oafrow.headOption.head.oafdistrict
                if (oafrow.headOption.head.oafstate.isDefined)
                  state = oafrow.headOption.head.oafstate
                if (oafrow.headOption.head.oafcountry.isDefined)
                  country = oafrow.headOption.head.oafcountry

                address = new Address(amtid, addressType, address1, address2, pin, city, district, state, country, landmark)
                userNominee = new Nominee(ndtid, nomineeName, nomineeRelation.map(_.toString), dobString, Some(address))
                userNominee
              } else{
                userNominee
              }
            })
          }
          else {
            Future{userNominee}
          }
        })
      }
      else {
        Future{userNominee}
      }
    })
  }

  /********************SAVE USER DETAILS****************************/
  def saveUserBasic(userid: Long, username: String, userBasic: UserBasic): Future[Boolean] = {
    var _dobDate: Option[java.sql.Date] = None
    var _ubdpan = Some(userBasic.pan.getOrElse("").toUpperCase)
    if (userBasic.dob.isDefined) {
      val formats = DateTimeUtils.convertDateToFormat(userBasic.dob.getOrElse(""), YYYYMMDD)
      if (!formats.isEmpty)
        _dobDate = Some(new java.sql.Date(formats.get.getTime))
    }
    var _maritalstatus: Option[Long] = None
    var _ubdmiddlename, _mob, _tel, _fax: Option[String] =  None
    var _ubdcontact: Option[Contact] = None

    var _ubdpanname = userBasic.panName
    var _ubdfirstname = userBasic.firstName
    var _ubdlastname = userBasic.lastName
    var _updfathername = userBasic.fatherName
    var _updmothername = userBasic.motherName
    var _ubdgender = userBasic.gender
    if (userBasic.maritalStatus.isDefined && userBasic.maritalStatus.get.length>0)
        _maritalstatus = Option(userBasic.maritalStatus.get.toLong)
    if (!userBasic.contact.isEmpty) {
      _ubdcontact = userBasic.contact
      _mob = _ubdcontact.get.mob
      _tel = _ubdcontact.get.tel
      _fax = _ubdcontact.get.fax
    }
    if (!userBasic.middleName.isEmpty) {
      _ubdmiddlename = userBasic.middleName
    }

    fcubdRepo.filter(x => x.id === userid).flatMap { value =>
      var row = value.headOption.get
      /*row = row.copy(ubdpan = _ubdpan, ubdpanname = _ubdpanname, ubdmobileno = _mob, ubddob = _dobDate, ubdgender = _ubdgender,
        ubdteleno = _tel, ubdfaxno = _fax, ubdfirstname = _ubdfirstname, ubdlastname = _ubdlastname, ubdmiddlename = _ubdmiddlename,
        ubdcndmaritalstatusrfnum = _maritalstatus)
     */
      var fcupdRow = new FcupdRowWrapper(None, None , _updfathername, _updmothername, None, None, None, userid, None, None).get(username)
      fcupdRepo.filter(x => x.updubdrfnum === userid).flatMap(updrow => {
        if (updrow.headOption.nonEmpty){
          var query = for {
            ubdNewInstance <- Fcubd.filter(x => x.id === userid).map(x => (x.ubdpan, x.ubdpanname, x.ubdmobileno, x.ubddob, x.ubdgender,
              x.ubdteleno, x.ubdfaxno, x.ubdfirstname, x.ubdlastname, x.ubdmiddlename, x.ubdcndmaritalstatusrfnum)).update((_ubdpan, _ubdpanname, _mob,
              _dobDate, _ubdgender, _tel, _fax, _ubdfirstname, _ubdlastname, _ubdmiddlename, _maritalstatus))
            updInstance <- Fcupd.filter(x => x.updubdrfnum === userid).map(x => (x.updfathername, x.updmothername)).update((_updfathername, _updmothername))
          } yield ()
          db.run(query).map(values => {
            true
          }).recover {
            case ex =>
              logger.debug(ex.getMessage)
              throw new Exception("Error occured while Updateing UBD & UPD")
          }
        }
        else {
          var query = for {
            ubdNewInstance <- Fcubd.filter(x => x.id === userid).map(x => (x.ubdpan, x.ubdpanname, x.ubdmobileno, x.ubddob, x.ubdgender,
              x.ubdteleno, x.ubdfaxno, x.ubdfirstname, x.ubdlastname, x.ubdmiddlename, x.ubdcndmaritalstatusrfnum)).update((_ubdpan, _ubdpanname, _mob,
              _dobDate, _ubdgender, _tel, _fax, _ubdfirstname, _ubdlastname, _ubdmiddlename, _maritalstatus))
            updNewInstance <- Fcupd returning Fcupd.map(_.id) into ((updObj, id) => updObj.copy(id = id)) += fcupdRow
          } yield ()
          db.run(query).map(values => {
            true
          }).recover {
            case ex =>
              logger.debug(ex.getMessage)
              throw new Exception("Error occured while Updating UBD & inserting in UPD")
          }
        }
      })
    }
  }

  def saveUserBanks(userid: Long, username: String, userBank: UserBank): Future[String] = {
    var bmtrfnum: Long = 1
    var bbtrfnum: Long = 1
    var buarfnum: Long = 0
    var _branchid, buaid: Option[Long] = None
    var _accountType:Long = CND_ACCTYPE_SAVINGS.toLong
    var _bankid, _ifsc, _city, _district, _state, _accountNumber, _holderName = ""

    if (userBank.buaid.isDefined && userBank.buaid.get.length>0)
      buaid = Option(userBank.buaid.get.toLong)
    if (userBank.accountType.isDefined && userBank.accountType.get.length>0){
      _accountType = userBank.accountType.get.toLong
    }
    if (userBank.accountNumber.isDefined)
      _accountNumber = userBank.accountNumber.get
    if (userBank.holderName.isDefined)
      _holderName = userBank.holderName.get.toUpperCase
    if (userBank.bank.isDefined){
      if (userBank.bank.get.bankName.isDefined)
        _bankid = userBank.bank.get.bankName.get
      if (userBank.bank.get.branch.isDefined && userBank.bank.get.branch.get.length>0)
        _branchid = Option(userBank.bank.get.branch.get.toLong)
      if (userBank.bank.get.IFSC.isDefined)
        _ifsc = userBank.bank.get.IFSC.get
      if (userBank.bank.get.city.isDefined)
        _city = userBank.bank.get.city.get
      if (userBank.bank.get.district.isDefined)
        _district = userBank.bank.get.district.get
      if (userBank.bank.get.state.isDefined)
        _state = userBank.bank.get.state.get
    }


    var fcbmtRowWrapper = new FcbmtRowWrapper(None, _bankid, None, Y_FLAG,None,None,N_FLAG)
    var fcbbtRowWrapper = new FcbbtRowWrapper(None, bmtrfnum, _branchid.map(_.toString), Y_FLAG, None, _ifsc, Option(_district), Option(_state), None)
    val fcbuaRowWrapper = new FcbuaRowWrapper(None, userid, bbtrfnum, _holderName, _accountNumber, _accountType, None, N_FLAG, UNAPPORVED_FLAG, None)

    if (buaid.isEmpty && _branchid.nonEmpty){
      var query = for{
        buaNewInstance <- Fcbua returning Fcbua.map(_.id) into ((buaObj, id) => buaObj.copy(id = id)) += (fcbuaRowWrapper.get(username).copy(buaubdrfnum = userid, buabbtrfnum = _branchid.get))
      } yield (buaNewInstance)
      db.run(query.transactionally).map(values => {
        values.id.toString
      }).recover {
        case ex: Exception => {
          logger.error("{}", ex)
          throw ex
        }
      }
    }
    else if(_branchid.nonEmpty) {
      var query = for {
        buaInstance <- Fcbua.filter(x => x.id === buaid.get).map(x => (x.buabbtrfnum, x.buaholdername, x.buacndaccttyperfnum, x.buaacctno)).update((_branchid.get, _holderName, _accountType, _accountNumber))
      } yield ()
      db.run(query.transactionally).map(values => {
        buaid.get.toString
      }).recover {
        case ex: Exception => {
          logger.error("{}", ex)
          throw ex
        }
      }
    }
    else {
      var query = for {
        buaInstance <- Fcbua.filter(x => x.id === buaid.get).map(x => (x.buaholdername, x.buacndaccttyperfnum, x.buaacctno)).update((_holderName, _accountType, _accountNumber))
      } yield ()
      db.run(query.transactionally).map(values => {
        buaid.get.toString
      }).recover {
        case ex: Exception => {
          logger.error("{}", ex)
          throw ex
        }
      }
    }
  }

  def saveUserAddresses(userid: Long, username: String, userAddress: UserAddress, userBasic: UserBasic): (String, String) = {
    var _ubdcontact: Option[Contact] = None
    var _mob: Option[String] = None
    var _tel: Option[String] = None
    var _fax: Option[String] = None
    var c, p = ""
    if (!userBasic.contact.isEmpty) {
      _ubdcontact = userBasic.contact
      _mob = _ubdcontact.get.mob
      _tel = _ubdcontact.get.tel
      _fax = _ubdcontact.get.fax
    }
    var _statec, _countryc, _countryp, _statep, _amtC, _amtP: Option[Long] = None
    var _address1c, _address2c, _landmarkc, _cityc, _pinc, _districtc, _address1p, _address2p, _landmarkp, _cityp, _pinp, _districtp = Option("")
    var _addressTypeC, _addressTypeP = Option("")
    if (userAddress.currentAddress.isDefined) {
      val _currentAddress = userAddress.currentAddress
      if(_currentAddress.get.amtid.isDefined && _currentAddress.get.amtid.get.length>0)
        _amtC = Option(_currentAddress.get.amtid.get.toLong)
      if(_currentAddress.get.addressType.isDefined)
        _addressTypeC = _currentAddress.get.addressType
      if(_currentAddress.get.address1.isDefined)
        _address1c = _currentAddress.get.address1
      _address2c = _currentAddress.get.address2
      _landmarkc = _currentAddress.get.landmark
      if (_currentAddress.get.state.isDefined && _currentAddress.get.state.get.length>0)
        _statec = Option(_currentAddress.get.state.get.toLong)
      if (_currentAddress.get.country.isDefined && _currentAddress.get.country.get.length>0)
        _countryc = Option(_currentAddress.get.country.get.toLong)
      _cityc = _currentAddress.get.city
      _pinc = _currentAddress.get.pin
      _districtc = _currentAddress.get.district
    }
    if (userAddress.permanentAddress.isDefined){
      val _permanentAddress = userAddress.permanentAddress
      if(_permanentAddress.get.amtid.isDefined && _permanentAddress.get.amtid.get.length>0)
        _amtP = Option(_permanentAddress.get.amtid.get.toLong)
      if(_permanentAddress.get.addressType.isDefined)
        _addressTypeP = _permanentAddress.get.addressType
      if(_permanentAddress.get.address1.isDefined)
        _address1p = _permanentAddress.get.address1
      _address2p = _permanentAddress.get.address2
      _landmarkp = _permanentAddress.get.landmark
      if (_permanentAddress.get.state.isDefined && _permanentAddress.get.state.get.length>0)
        _statep = Option(_permanentAddress.get.state.get.toLong)
      if (_permanentAddress.get.country.isDefined && _permanentAddress.get.country.get.length>0)
        _countryp = Option(_permanentAddress.get.country.get.toLong)
      _cityp = _permanentAddress.get.city
      _pinp = _permanentAddress.get.pin
      _districtp = _permanentAddress.get.district

    }

    /// Current Address
    var fcamtRowWrapperC = new FcamtRowWrapper(None, _addressTypeC.get, _address1c.get, _address2c, None, _landmarkc, _mob, _tel, Some(username),
      None, _fax, None, _statec, _countryc, None, None, None)
    var fcoafRowC = new FcoafRow(0, 0, None, None, _districtc, _cityc, _pinc, None)
    var fcuaaRowC = new FcuaaRowWrapper(None, userid, 0, CND_ADDRESSTYPE_BUSINESSRESIDENTIAL.toLong, CND_CONTACTADDRESS_CORRESPONDENCE.toLong, None, None, None, None, DOC_UNDERPROCESS).get(username)

    /// Permanent Address
    var fcamtRowWrapperP = new FcamtRowWrapper(None, _addressTypeP.get, _address1p.get, _address2p, None, _landmarkp, _mob, _tel, Some(username),
      None, _fax, None, _statep, _countryp, None, None, None)
    var fcoafRowP = new FcoafRow(0, 0, None, None, _districtp, _cityp, _pinp, None)
    var fcuaaRowP = new FcuaaRowWrapper(None, userid, 0, CND_ADDRESSTYPE_BUSINESSRESIDENTIAL.toLong, CND_CONTACTADDRESS_PERMANENT.toLong, None, None, None, None, DOC_UNDERPROCESS).get(username)

    // New User
    if (_amtP.isEmpty){
      // Same Address
      if (userAddress.permanentEqualsCurrent){
        var query = for {
          amtNewInstance <- Fcamt returning Fcamt.map(_.id) into ((amtObj, id) => amtObj.copy(id = id)) += (fcamtRowWrapperP.get(username))
          oafNewInstance <- Fcoaf returning Fcoaf.map(_.id) into ((oafObj, id) => oafObj.copy(id = id)) += (fcoafRowP.copy(oafamtrfnum = amtNewInstance.id))
          uaainstanceP <- Fcuaa returning Fcuaa.map(_.id) into ((uaaObj, id) => uaaObj.copy(id = id)) += (fcuaaRowP.copy(uaaamtrfnum = amtNewInstance.id))
          uaainstanceC <- Fcuaa returning Fcuaa.map(_.id) into ((uaaObj, id) => uaaObj.copy(id = id)) += (fcuaaRowC.copy(uaaamtrfnum = amtNewInstance.id))
        } yield(amtNewInstance)
        var three = db.run(query.transactionally).map(values => {
          p = values.id.toString
          c = values.id.toString
        }).recover {
          case ex: Exception =>
            logger.error("{}", ex)
            throw ex
        }
        Await.result(three, Duration.Inf)
      }
      else {
        var query = for {
          amtNewInstanceP <- Fcamt returning Fcamt.map(_.id) into ((amtObj, id) => amtObj.copy(id = id)) += (fcamtRowWrapperP.get(username))
          amtNewInstanceC <- Fcamt returning Fcamt.map(_.id) into ((amtObj, id) => amtObj.copy(id = id)) += (fcamtRowWrapperC.get(username))
          oafNewInstanceP <- Fcoaf returning Fcoaf.map(_.id) into ((oafObj, id) => oafObj.copy(id = id)) += (fcoafRowP.copy(oafamtrfnum = amtNewInstanceP.id))
          oafNewInstanceC <- Fcoaf returning Fcoaf.map(_.id) into ((oafObj, id) => oafObj.copy(id = id)) += (fcoafRowC.copy(oafamtrfnum = amtNewInstanceC.id))
          uaainstanceP <- Fcuaa returning Fcuaa.map(_.id) into ((uaaObj, id) => uaaObj.copy(id = id)) += (fcuaaRowP.copy(uaaamtrfnum = amtNewInstanceP.id))
          uaainstanceC <- Fcuaa returning Fcuaa.map(_.id) into ((uaaObj, id) => uaaObj.copy(id = id)) += (fcuaaRowC.copy(uaaamtrfnum = amtNewInstanceC.id))
        } yield(amtNewInstanceP, amtNewInstanceC)
        var three = db.run(query.transactionally).map(values => {
          p = values._1.id.toString
          c = values._2.id.toString
        }).recover {
          case ex: Exception =>
            logger.error("{}", ex)
            throw ex
        }
        Await.result(three, Duration.Inf)
      }
    }
    // Returning user
    else {
      if (userAddress.permanentEqualsCurrent){
        var query = for {
          amtInstance <- Fcamt.filter(x => x.id === _amtP.get).map(x => (x.amtaddtype, x.amtaddline1, x.amtaddline2, x.amtaddlandmark, x.amtmobileno,
            x.amttelephoneno, x.amtfax, x.amtcndstaterfnum, x.amtcndcountryrfnum)).update((_addressTypeP.get, _address1p.get, _address2p, _landmarkp, _mob, _tel, _fax, _statep, _countryp))
          oafInstance <- Fcoaf.filter(x => x.oafamtrfnum === _amtP.get).map(x => (x.oafcity, x.oafdistrict, x.oafpin)).update((_cityp, _districtp, _pinp))
          uaaInstanceC <- Fcuaa.filter(x => x.uaaubdrfnum === userid && x.uaacndcontactaddress === CND_CONTACTADDRESS_CORRESPONDENCE.toLong).map(x => (x.uaaamtrfnum)).update((_amtP.get))
        } yield()
        var three = db.run(query.transactionally).map(values => {
          p = _amtP.get.toString
          c = _amtP.get.toString
        }).recover {
          case ex: Exception =>
            logger.error("{}", ex)
            throw ex
        }
        Await.result(three, Duration.Inf)
      }
      else {
        if (_amtP.get == _amtC.get){
          var query = for {
            amtNewInstanceC <- Fcamt returning Fcamt.map(_.id) into ((amtObj, id) => amtObj.copy(id = id)) += (fcamtRowWrapperC.get(username))
            amtInstanceP <- Fcamt.filter(x => x.id === _amtP.get).map(x => (x.amtaddtype, x.amtaddline1, x.amtaddline2, x.amtaddlandmark, x.amtmobileno,
              x.amttelephoneno, x.amtfax, x.amtcndstaterfnum, x.amtcndcountryrfnum)).update((_addressTypeP.get, _address1p.get, _address2p, _landmarkp, _mob, _tel, _fax, _statep, _countryp))
            oafNewInstanceC <- Fcoaf returning Fcoaf.map(_.id) into ((oafObj, id) => oafObj.copy(id = id)) += (fcoafRowC.copy(oafamtrfnum = amtNewInstanceC.id))
            oafInstance <- Fcoaf.filter(x => x.oafamtrfnum === _amtP.get).map(x => (x.oafcity, x.oafdistrict, x.oafpin)).update((_cityp, _districtp, _pinp))
            uaaInstanceC <- Fcuaa.filter(x => x.uaaubdrfnum === userid && x.uaacndcontactaddress === CND_CONTACTADDRESS_CORRESPONDENCE.toLong).map(x => (x.uaaamtrfnum)).update((amtNewInstanceC.id))
          } yield (amtNewInstanceC)
          var three = db.run(query.transactionally).map(values => {
            p = _amtP.get.toString
            c = values.id.toString
          }).recover {
            case ex: Exception =>
              logger.error("{}", ex)
              throw ex
          }
          Await.result(three, Duration.Inf)
        }
        else {
          var query = for {
            amtInstanceP <- Fcamt.filter(x => x.id === _amtP.get).map(x => (x.amtaddtype, x.amtaddline1, x.amtaddline2, x.amtaddlandmark, x.amtmobileno,
              x.amttelephoneno, x.amtfax, x.amtcndstaterfnum, x.amtcndcountryrfnum)).update((_addressTypeP.get, _address1p.get, _address2p, _landmarkp, _mob, _tel, _fax, _statep, _countryp))
            amtInstance <- Fcamt.filter(x => x.id === _amtC.get).map(x => (x.amtaddtype, x.amtaddline1, x.amtaddline2, x.amtaddlandmark, x.amtmobileno,
              x.amttelephoneno, x.amtfax, x.amtcndstaterfnum, x.amtcndcountryrfnum)).update((_addressTypeC.get, _address1c.get, _address2c, _landmarkc, _mob, _tel, _fax, _statec, _countryc))
            oafInstance <- Fcoaf.filter(x => x.oafamtrfnum === _amtP.get).map(x => (x.oafcity, x.oafdistrict, x.oafpin)).update((_cityp, _districtp, _pinp))
            oafInstanceC <- Fcoaf.filter(x => x.oafamtrfnum === _amtC.get).map(x => (x.oafcity, x.oafdistrict, x.oafpin)).update((_cityc, _districtc, _pinc))
          } yield()
          var three = db.run(query.transactionally).map(values => {
            p = _amtP.get.toString
            c = _amtC.get.toString
          }).recover {
            case ex: Exception =>
              logger.error("{}", ex)
              throw ex
          }
          Await.result(three, Duration.Inf)
        }
      }
    }
    (p, c)
  }

  def saveUserFatca(userid: Long, username: String, userFatca: UserFatca): Future[String] = {
    var _futid: Option[Long] = None
    var _isIndianResident = Y_FLAG
    var _birthCountry, _nationality, _taxCountry: Option[Long] = Some(CND_COUNTRY_INDIA.toLong)
    var _income, _occupation, _sourceOfWealth: Option[Long] = None
    if (userFatca.futid.isDefined && userFatca.futid.get.length>0)
      _futid = Some(userFatca.futid.get.toLong)
    if (userFatca.nationality.isDefined && userFatca.nationality.get.length>0) {
      _nationality = Some(userFatca.nationality.get.toLong)
    }
    if (userFatca.birthCountry.isDefined && userFatca.birthCountry.get.length>0) {
      _birthCountry = Some(userFatca.birthCountry.get.toLong)
    }
    if (userFatca.taxCountry.isDefined && userFatca.taxCountry.get.length>0) {
      _taxCountry = Some(userFatca.taxCountry.get.toLong)
    }
    if (_nationality != CND_COUNTRY_INDIA) {
      _isIndianResident = N_FLAG
    }
    if (userFatca.income.isDefined && userFatca.income.get.length>0) {
      _income = Some(userFatca.income.get.toLong)
    }
    if (userFatca.occupation.isDefined && userFatca.occupation.get.length>0) {
      _occupation = Some(userFatca.occupation.get.toLong)
    }
    if (userFatca.sourceOfWealth.isDefined && userFatca.sourceOfWealth.get.length>0){
      _sourceOfWealth = Some(userFatca.sourceOfWealth.get.toLong)
    }
    var _isCorporate = N_FLAG

    var fcfutRowWrapper = new FcfutRowWrapper(None, userid, None, None, _birthCountry, _taxCountry,
      Some(_isIndianResident), userFatca.birthCity, None, None, _income, None, userFatca.politicallyExposed, userFatca.politicallyRelated, None, None, Some(_isCorporate), None)
    var fcefutRowWrapper = new FcefutRowWrapper(1, Option(N_FLAG), _sourceOfWealth)

    if (_futid.isEmpty){
      fcfutRepo.filter(x => x.futubdrfnum === userid).flatMap(value => {
        if (value.headOption.isEmpty) {
          var query = for {
            futNewInstance <- Fcfut returning Fcfut.map(_.id) into ((futObj, id) => futObj.copy(id = id)) += (fcfutRowWrapper.get(username))
            _ <- Fcefut += (fcefutRowWrapper.get(username).copy(id = futNewInstance.id))
            ubdrow <- Fcubd.filter(x => x.id === userid).map(x => (x.ubdcndoccupationrfnum, x.ubdcndnationalityrfnum, x.lastmodifiedby, x.modifydate)).update((_occupation, _nationality, username, DateTimeUtils.getCurrentTimeStamp()))
          } yield (futNewInstance)
          val retval = db.run(query.transactionally).map(values => {
            values.id.toString
          }).recover {
            case ex: Exception =>
              logger.error("{}", ex)
              throw ex
          }
          retval
        } else {
            val _newfutid = value.head.id;
            var query = for {
              futrow <- Fcfut.filter(x => x.id === _newfutid).map(x => (x.futcndbirthcountryrfnum, x.futcndtaxresidentrfnum, x.futisindiaresident,
                x.futbirthcity, x.futcndincomeslabrfnum, x.futpoliticallyexposed, x.futpoliticallyrelated, x.futiscorporate, x.lastmodifiedby, x.modifydate)).update(_birthCountry,
                _taxCountry, Some(_isIndianResident), userFatca.birthCity, _income, userFatca.politicallyExposed, userFatca.politicallyRelated, Some(_isCorporate), username, DateTimeUtils.getCurrentTimeStamp())
              efutrow <- Fcefut.filter(x => x.id === _newfutid).map(x => (x.efutcndwealthsource, x.lastmodifiedby, x.modifydate)).update((_sourceOfWealth, username, DateTimeUtils.getCurrentTimeStamp()))
              ubdrow <- Fcubd.filter(x => x.id === userid).map(x => (x.ubdcndoccupationrfnum, x.ubdcndnationalityrfnum, x.lastmodifiedby, x.modifydate)).update((_occupation, _nationality, username, DateTimeUtils.getCurrentTimeStamp()))
            } yield()
            val retval = db.run(query).map(values => {
              _newfutid.toString
            }).recover {
              case ex: Exception =>
                logger.error("{}", ex)
                throw ex
            }
            retval
          }
        })
      }else {
      var query = for {
        futrow <- Fcfut.filter(x => x.id === _futid).map(x => (x.futcndbirthcountryrfnum, x.futcndtaxresidentrfnum, x.futisindiaresident,
          x.futbirthcity, x.futcndincomeslabrfnum, x.futpoliticallyexposed, x.futpoliticallyrelated, x.futiscorporate, x.lastmodifiedby, x.modifydate)).update(_birthCountry,
          _taxCountry, Some(_isIndianResident), userFatca.birthCity, _income, userFatca.politicallyExposed, userFatca.politicallyRelated, Some(_isCorporate), username, DateTimeUtils.getCurrentTimeStamp())
        efutrow <- Fcefut.filter(x => x.id === _futid).map(x => (x.efutcndwealthsource, x.lastmodifiedby, x.modifydate)).update((_sourceOfWealth, username, DateTimeUtils.getCurrentTimeStamp()))
        ubdrow <- Fcubd.filter(x => x.id === userid).map(x => (x.ubdcndoccupationrfnum, x.ubdcndnationalityrfnum, x.lastmodifiedby, x.modifydate)).update((_occupation, _nationality, username, DateTimeUtils.getCurrentTimeStamp()))
      } yield()
      db.run(query).map(values => {
        _futid.get.toString
      }).recover {
        case ex: Exception =>
          logger.error("{}", ex)
          throw ex
      }
    }
  }

  def saveUserNominee(userid: Long, username: String, userNominee: Nominee): Future[String] = {
    var _addressType, _address1, _address2, _landmark, _city, _district, _pin, _region: Option[String] = None
    var _ndtid, _amtid, _state, _country, _nomineeRelation: Option[Long] = None
    var _nomineeDob: Option[java.sql.Date] = None

    if (userNominee.nomineeDob.isDefined) {
      val formats = DateTimeUtils.convertDateToFormat(userNominee.nomineeDob.getOrElse(""), YYYYMMDD)
      if (!formats.isEmpty)
        _nomineeDob = Some(new java.sql.Date(formats.get.getTime))
    }
    if (userNominee.ndtid.isDefined && userNominee.ndtid.get.length>0)
      _ndtid = Option(userNominee.ndtid.get.toLong)
    if (userNominee.nomineeRelation.isDefined && userNominee.nomineeRelation.get.length>0)
      _nomineeRelation = Option(userNominee.nomineeRelation.get.toLong)
    var _nomineeName = userNominee.nomineeName
    if (!userNominee.nomineeAddress.isEmpty) {
      val _nomineeAddress = userNominee.nomineeAddress
      if (_nomineeAddress.get.amtid.isDefined && _nomineeAddress.get.amtid.get.length>0)
        _amtid = Option(_nomineeAddress.get.amtid.get.toLong)
      if (_nomineeAddress.get.state.isDefined && _nomineeAddress.get.state.get.length>0)
        _state = Option(_nomineeAddress.get.state.get.toLong)
      if (_nomineeAddress.get.country.isDefined && _nomineeAddress.get.country.get.length>0)
        _country = Option(_nomineeAddress.get.country.get.toLong)
      _addressType = _nomineeAddress.get.addressType
      _address1 = _nomineeAddress.get.address1
      _address2 = _nomineeAddress.get.address2
      _landmark = _nomineeAddress.get.landmark
      _city = _nomineeAddress.get.city
      _district = _nomineeAddress.get.district
      _pin = _nomineeAddress.get.pin
    }

    var fcamtRowWrapper = new FcamtRowWrapper(None, _addressType.get, _address1.get, _address2, None, _landmark, None, None, None, None, None, None, _state, _country, None, None, None)
    var fcoafRowWrapper = new FcoafRowWrapper(None, 0, None, None,_district, _city, _pin, _region)
    var fcndtRowWrapper = new FcndtRowWrapper(None, _nomineeName, _nomineeRelation, _nomineeDob, None, userid)

    if (_ndtid.isEmpty) {
      val query = for {
        amtNewInstance <- Fcamt returning Fcamt.map(_.id) into ((amtObj, id) => amtObj.copy(id = id)) += (fcamtRowWrapper.get(username))
        oafNewInstance <- Fcoaf returning Fcoaf.map(_.id) into ((oafObj, id) => oafObj.copy(id = id)) += (fcoafRowWrapper.get(username).copy(oafamtrfnum = amtNewInstance.id))
        ndtNewInstance <- Fcndt returning Fcndt.map(_.id) into ((uaaObj, id) => uaaObj.copy(id = id)) += (fcndtRowWrapper.get(username).copy(ndtamtaddressrfnum = Option(amtNewInstance.id)))
      } yield (ndtNewInstance)
      db.run(query.transactionally).map(values => {
        values.id.toString
      }).recover {
        case ex: Exception =>
          logger.error("{}", ex)
          throw ex
      }
    }
    else {
      var query = for {
        amtInstance <- Fcamt.filter(x => x.id === _amtid.get).map(x => (x.amtaddtype, x.amtaddline1, x.amtaddline2, x.amtaddlandmark, x.amtcndstaterfnum, x.amtcndcountryrfnum)).update((_addressType.get, _address1.get, _address2, _landmark, _state, _country))
        oafInstance <- Fcoaf.filter(x => x.oafamtrfnum === _amtid.get).map(x => (x.oafcity, x.oafdistrict, x.oafpin)).update((_city, _district, _pin))
        ndtInstance <- Fcndt.filter(x => x.id === _ndtid.get).map(x => (x.ndtnomineename, x.ndtcndrelationshiprfnum, x.ndtdob)).update((_nomineeName, _nomineeRelation, _nomineeDob))
      } yield ()
      db.run(query.transactionally).map(values => {
        _ndtid.get.toString
      }).recover {
        case ex: Exception =>
          logger.error("{}", ex)
          throw ex
      }
    }
  }

  def saveUserAadhar(userid: Long, username: String, userAadhar: EKycApiData) : Future[Boolean]= {

    var fceubdRowWrapper = new FceubdRowWrapper(userid, userAadhar.aadhar, N_FLAG, None, N_FLAG,Some(N_FLAG), N_FLAG)

    var query = Fceubd.insertOrUpdate(fceubdRowWrapper.get(username))
    db.run(query).map(values => {
      true
    }).recover {
        case ex =>
          logger.debug(ex.getMessage)
          throw new Exception("Error occured while querying by id")
      }
  }

  /***********************FOLIO Number API**************************/
  def getNumberOfFoliosByUserPk(userid: Long): Future[Int] = {
    fcuftRepo.filter(x => x.uftubdrfnum === userid).map(y => y.length)
  }

  def getUserNomineeDetails(userName: String): Future[Seq[(String, String)]] = {

    val query = for {
      ubdObj <- Fcubd.filter(_.ubdemailid === userName).map(_.id)
      ndtObj <- Fcndt.filter(_.ndtubdrfnum === ubdObj).map(x => (x.ndtnomineename, x.ndtcndrelationshiprfnum))
      cndObj <- Fccnd.filter(_.id === ndtObj._2).map(_.cndname)
    } yield (ndtObj._1.get, cndObj)

    db.run(query.result).map(values => {
      values
    })
  }

  def getUserBankDetails(userName: String): Future[List[FcbmtRow]] = {
    val query = for {
      bmtObj <- Fcbmt.filter(x => {
        x.id in (Fcbbt.filter(_.id in (Fcbua.filter(_.buaubdrfnum in Fcubd.filter(_.ubdemailid === userName).map(_.id)).map(_.buabbtrfnum))).map(_.bbtbmtrfnum))
      })
    } yield (bmtObj)

    db.run(query.result).map(values => {
      values.toList
    })
  }

  /***********************OTP APIs***********************************/
  def saveOTPDetails(userId: Long, otp: String, gatewayId: String, purpose: String, mobileNo: String, ip:String): Future[Long] = {

    fcubdRepo.filter(x => x.id === userId).flatMap(ubdRowList => {
      val ubdRow = ubdRowList.head
      val currentTime = DateTimeUtils.getCurrentTimeStamp()
      val validationTime = DateTimeUtils.getTimestampAfterInterval(OTP_VALID_TIME)
      val fcotptRow = new FcotptRowWrapper(None, mobileNo, otp, Some(currentTime), Some(validationTime), None, None, gatewayId, Y_FLAG,Some(ip)).get(ubdRow.ubdemailid)
      val fcuoaRow = new FcuoaRowWrapper(None, ubdRow.id, purpose, 0).get(ubdRow.ubdemailid)

      val query = for {
        fcotpt <- Fcotpt returning Fcotpt.map(_.id) into ((fcotptObj, id) => fcotptObj.copy(id = id)) += fcotptRow
        fcuoa <- Fcuoa returning Fcuoa.map(_.id) into ((fcuoaObj, id) => fcuoaObj.copy(id = id)) += fcuoaRow.copy(uoaotptrfnum = fcotpt.id)
      } yield (fcotpt.id)

      db.run(query.transactionally).map(value => {
        value
      })
    })
  }

  def updateOTPMessageId(messageId: String, otprfnum: Long, userName: String): Future[Int] = {

    val query = Fcotpt.filter(_.id === otprfnum).map(x => (x.otptmessageid, x.modifydate, x.lastmodifiedby))
      .update((Some(messageId), DateTimeUtils.getCurrentTimeStamp(), userName))
    db.run(query).map(value => value)
  }

  def validateOTP(otp: String, purpose: String, userId: Long): Future[Seq[FcotptRow]] = {

    val query = for{
      uoaRowList <- Fcuoa.filter(x => x.uoaubdrfnum === userId && x.uoapurpose === purpose).map(_.uoaotptrfnum).result
      otptRow <- Fcotpt.filter(x => x.otptotp === otp && x.otptisvalid === Y_FLAG &&
                                x.otptvaliditytime >= DateTimeUtils.getCurrentTimeStamp() && (x.id inSetBind(uoaRowList))).forUpdate.result

      updatedObj <- otptRow.size match{
        case 0 => DBIO.successful(1)
        case n => Fcotpt.filter(_.id === otptRow.head.id).map(_.otptisvalid).update(N_FLAG)
      }

    }yield(otptRow)

    db.run(query.transactionally).map(values => {
      values
    })


  }

  def updateOTPStatus(messageId: String, otpStatus: String): Future[Int] = {

    val query = Fcotpt.filter(_.otptmessageid === messageId).map(x => (x.otptstatus, x.modifydate, x.lastmodifiedby))
      .update(Some(otpStatus), DateTimeUtils.getCurrentTimeStamp(), "SYSTEM")
    db.run(query).map(values => {
      values
    })
  }

  def getMessageStatus(messageId: String): Future[String] = {
    fcotptRepo.filter(x => {
      x.otptmessageid === messageId
    }).map(x => {
      val row = x.headOption
      if (row.isEmpty) {
        ""
      } else {
        row.get.otptstatus.getOrElse("")
      }
    })
  }

  /*def getMailLinkValidityStatus(mailLinkId: String): Future[String] = {
    fcuactRepo.filter(x => {
      x.U === mailLinkId
    }).map(x => {
      val row = x.headOption
      if (row.isEmpty) {
        ""
      } else {
        row.get.otpstatus.getOrElse("")
      }
    })
  }*/

}