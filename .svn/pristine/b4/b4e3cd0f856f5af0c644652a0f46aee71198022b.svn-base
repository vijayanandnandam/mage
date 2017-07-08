package repository.module

import com.google.inject.Inject
import constants.{BaseConstants, IntegrationConstants}
import data.model.Tables.{Fcdrqp, Fcimt, FcimtRow, Fcirql, FcirqlRow, FcirqlRowWrapper, Fcirsl, FcirslRow, FcirslRowWrapper, FcsiaRow}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repository.tables.{FcdrqpRepo, FcimtRepo, FcsiaRepo}
import slick.jdbc.JdbcProfile

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by fincash on 21-01-2017.
  */
class IntegrationRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,fcsiaRepo: FcsiaRepo, fcimtRepo: FcimtRepo, fcdrqpRepo: FcdrqpRepo) extends IntegrationConstants with BaseConstants with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def getBSEIntegration(): Future[FcimtRow] = {
    fcimtRepo.filter(x => x.imtintgratname === BSE_INTEGRATION_NAME).map {
      x => x.head
    }
  }

  def getIntegrationNum(IntegrationKey: String): Future[Long] = {
    val query = Fcimt.filter(x => x.imtintgratname === IntegrationKey).map(_.id).result

    db.run(query).map { values =>
      values.headOption.getOrElse(-1L)
    }
  }

  def getIntegration(smtrfnum:Long,transType:String):Future[Seq[FcsiaRow]] = {

    fcsiaRepo.filter(x => x.siasmtrfnum === smtrfnum && x.siatxntype === transType)
  }

  def getBSEDefaultParamValue(paramName: String) = {

    fcdrqpRepo.filter(x => x.drqpparaname === paramName).map {
      x => x(0).drqpparavalue
    }
  }

  def getDefaultParamValues(paramNameList: ListBuffer[String], integrationName: String): Future[mutable.LinkedHashMap[String, String]] = {

    val query = for {
      integrationNum <- Fcimt.filter(x => x.imtintgratname === integrationName).map(_.id).result.headOption
      x <- Fcdrqp.filter(x => x.drqpimtrfnum === integrationNum && (x.drqpparaname inSetBind paramNameList)).map(x => (x.drqpparaname, x.drqpparavalue)).result
    } yield (x)

    val parameterMap = mutable.LinkedHashMap.empty[String, String]

    db.run(query).map { paramList =>
      paramList.foreach(param =>
        parameterMap += (param._1 -> param._2)
      )
      parameterMap
    }
  }

  def saveBSERequestParameters(integrationId: Option[String], paramNameList: ListBuffer[String], paramValueList: ListBuffer[String], userName: String) = {

    getIntegrationNum(BSE_INTEGRATION_NAME).map { integrationNum =>

      val imtRfNum = integrationNum
      val fcirqlTable = TableQuery[Fcirql]

      val totalParams = paramNameList.length

      val fcirqlRowList: ListBuffer[FcirqlRow] = ListBuffer[FcirqlRow]()
      for (index <- 0 until totalParams) {
        fcirqlRowList.+=(new FcirqlRowWrapper(None, imtRfNum, paramNameList(index), paramValueList(index), integrationId).get(userName))
      }

      db.run((fcirqlTable ++= fcirqlRowList).transactionally) map {
        x => x
      } recover {
        case ex => ex.printStackTrace()
          throw new Exception("Error Occured")
      }
    }
  }

  def saveBSEResponseParameters(integrationId: Option[String], paramNameList: ListBuffer[String], paramValueList: ListBuffer[String], userName: String) = {

    getIntegrationNum(BSE_INTEGRATION_NAME).map { integrationNum =>

      val imtRfNum = integrationNum
      val fcirslTable = TableQuery[Fcirsl]

      val totalParams = paramNameList.length

      val fcirslRowList: ListBuffer[FcirslRow] = ListBuffer[FcirslRow]()
      for (index <- 0 until totalParams) {
        fcirslRowList.+=(new FcirslRowWrapper(None, imtRfNum, integrationId,paramNameList(index), paramValueList(index )).get(userName))
      }

      db.run((fcirslTable ++= fcirslRowList).transactionally) map {
        x => x
      } recover {
        case ex => ex.printStackTrace()
          throw new Exception("Error Occured")
      }
    }
  }
}
