package controllers

import javax.inject.Inject

import helpers.AuthenticatedAction
import models.JsonFormats._
import models.{BMTRow, BankSuggestion, CNDRow}
import play.api.libs.json.Json
import play.api.mvc.Controller
import repository.module.{BankRepository, CNDRepository}
import service.SolrBankSearchService

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

/**
  * Created by Fincash on 08-02-2017.
  */
class LookupController @Inject() (implicit val ec: ExecutionContext, auth: AuthenticatedAction, cndRepository: CNDRepository,
                                  bankRepository: BankRepository, solrBankSearchService: SolrBankSearchService)
              extends Controller {

  def postCNDgroup = auth.Action.async(parse.json) { request =>
    val group = (request.body \ "group").as[String]
    getData(group)
  }

  def getCNDgroup(group: String) = auth.Action.async { request =>
    getData(group)
  }

  def getData(group : String) ={
    var cndList = ListBuffer[CNDRow]()
    cndRepository.getCndGroup(group).map(rows => {
      if (rows.isEmpty){
        Ok(Json.obj("error" -> true, "reason" -> "No result found"))
      }
      else {
        if(rows.nonEmpty){
          val _rows = rows.get
          for (row <- _rows) {
            cndList.+= (CNDRow(row.id.toString,row.cndname))
          }
          Ok(Json.toJson(cndList))
        }else{
          Ok(Json.obj("error" -> true, "reason" -> "No result found"))
        }
      }
    }).recover {
      case ex => Ok(Json.obj("error" -> true, "reason" -> ex.getMessage))
    }
  }

  def getBankNames = auth.Action.async { request => {
    var bmtList = ListBuffer[BMTRow]()
    bankRepository.getBankNames().map(rows => {
      if (rows.isEmpty){
        Ok(Json.obj("error" -> true, "reason" -> "No result found"))
      }
      else {
        for (row <- rows) {
          bmtList.+= (BMTRow(row.id.toString,row.bmtbankname))
        }
        Ok(Json.toJson(bmtList))
      }
    }).recover {
      case ex => Ok(Json.obj("error" -> true, "reason" -> ex.getMessage))
    }}
  }

  def getBankSuggestion = auth.Action(parse.json) {request => {
    var requestData = request.body
    var bankList = ListBuffer[BankSuggestion]()
    var term = (requestData \ "term").as[String]
    var banks = solrBankSearchService.autoComplete(term)
    Ok(Json.toJson(banks))
  }}
}
