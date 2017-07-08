package service

import javax.inject.Inject

import constants.DBConstants
import data.model.Tables.FccndRow
import repository.module.CNDRepository

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 31-01-2017.
  */
class CNDService @Inject()(implicit ec:ExecutionContext, cndRepository: CNDRepository) extends DBConstants{

  def getCNDByPk(id:Long):Future[Option[FccndRow]] = {
    cndRepository.getCnd(id)
  }

  def getCNDByCndName(cndName:String,cndGroup : Option[String]):Future[Option[FccndRow]] = {
    cndRepository.getCndByCndName(cndName,cndGroup)
  }

  def getBSECodeByCndPk(id:Long):Future[Option[String]] = {
    getCNDByPk(id).map(cndRow =>{
      if(cndRow.nonEmpty){
        cndRow.get.cndcode
      }else{
        None
      }
    })
  }

  def getBSEExtFld2ByCndPk(id:Long):Future[Option[String]] = {
    getCNDByPk(id).map(cndRow =>{
      if(cndRow.nonEmpty){
        cndRow.get.cndextfld2
      }else{
        None
      }
    })
  }

  def getBSECodeByCndName(cndName:String):Future[Option[String]] = {
    getCNDByCndName(cndName,None).map(cndRow =>{
      if(cndRow.nonEmpty){
        cndRow.get.cndcode
      }else{
        None
      }
    })
  }

  def getCndNameByCndPk(id:Long):Future[Option[String]] = {
    getCNDByPk(id).map(cndRow =>{
      if(cndRow.nonEmpty)
       Some(cndRow.get.cndname)
      else
        None
    })
  }

  def getCndExtField1ByCndPk(id:Long):Future[Option[String]] = {
    getCNDByPk(id).map(cndRow =>{
      if(cndRow.nonEmpty)
        cndRow.get.cndextfld1
      else
        None
    })
  }
}
