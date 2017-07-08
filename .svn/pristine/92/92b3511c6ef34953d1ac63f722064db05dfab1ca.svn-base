package repository.module

import javax.inject.Inject

import constants.IntegrationConstants
import data.model.Tables.{Fclpsa, FclpsaRow, Fcpst, FcpstRow}
import models.ProductStateObject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repository.tables.{FclpsaRepo, FcpstRepo}
import slick.jdbc.JdbcProfile
import slick.jdbc.GetResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by fincash on 31-01-2017.
  */
class ProductRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                                  fcpstRepo: FcpstRepo, fclpsaRepo: FclpsaRepo)
  extends IntegrationConstants with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def getProduct(id: Long): Future[FcpstRow] = {
    fcpstRepo.getById(id).map(_.head)
  }

  def getProductFunds(productId: Long): Future[Seq[FclpsaRow]] = {
    fclpsaRepo.filter(x => x.lpsapstrfnum === productId).map {
      x => x
    }
  }

  def getProductStateObjectList(productId: Long): Future[List[ProductStateObject]] = {

    implicit val getProductStateObject = GetResult(r => ProductStateObject(r.nextString, r.nextString, r.nextStringOption, r.nextStringOption, r.nextLongOption, r.nextStringOption, r.nextLongOption));
    val query = sql"""SELECT
    GROUP_CONCAT(sopt.SOPTRFNUM separator ',') as soptRfnumArray,
    GROUP_CONCAT(sopt.SOPTISDEFAULT separator ',') as soptIsDefaultArray,
    GROUP_CONCAT(lpsa.LPSAINVESTMENTMODE separator ',') as soptInvestmentModeArray,
    GROUP_CONCAT(lpsa.LPSAWEIGHTAGE separator ',') as soptIsWeightage,
    lpsa.LPSASMTRFNUM as smtRfnum,
    pst.PSTPRODUCTNAME as productName,
    pst.PSTRFNUM as productRfnum
FROM
    FCLPSA lpsa,
    FCSOPT sopt,
    FCPST pst
WHERE
	lpsa.LPSAPSTRFNUM = pst.PSTRFNUM
    AND lpsa.LPSASOPTRFNUM = sopt.SOPTRFNUM
    AND lpsa.LPSAPSTRFNUM = ${productId}
    GROUP BY lpsa.LPSASOPTRFNUM;""".as[ProductStateObject];

    db.run(query).map(values => values.toList);
  }


  def getProductByName(productName: String): Future[FcpstRow] = {
    fcpstRepo.filter(x => x.pstproductname === productName).map {
      x => x.head
    }
  }

  def getProductByFund(schemeId: Long): Future[Option[(Option[String], Long, String)]] = {
    val query = for {
      lpsaObj <- Fclpsa.filter(_.lpsasmtrfnum === schemeId)
      pstObj <- Fcpst.filter(_.id === lpsaObj.lpsapstrfnum)
    } yield (lpsaObj.lpsainvestmentmode, lpsaObj.lpsasoptrfnum, pstObj.pstproductname)

    db.run(query.result).map(x => {
      x.headOption
    })
  }
}
