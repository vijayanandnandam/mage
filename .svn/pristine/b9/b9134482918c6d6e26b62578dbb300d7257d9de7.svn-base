package controllers

import javax.inject.Inject

import org.slf4j.LoggerFactory
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}

import scala.util._


class Application @Inject() (implicit ws: WSClient, messagesApi: MessagesApi) extends Controller{
  val logger, log = LoggerFactory.getLogger(classOf[Application])

  def getJVMUsage = Action { request =>
    val mb = 1024*1024

    //Getting the runtime reference from system
    val runtime = Runtime.getRuntime()

    println("##### Heap utilization statistics [MB] #####")

    //Print used memory
    val usedMemory = (runtime.totalMemory() - runtime.freeMemory())/mb
    println("Used Memory:"
      + (runtime.totalMemory() - runtime.freeMemory()) / mb)

    //Print free memory
    val freeMemory = runtime.freeMemory() / mb
    println("Free Memory:"
      + runtime.freeMemory() / mb)

    //Print total available memory
    val totalMemorty = runtime.totalMemory() / mb
    println("Total Memory:" + runtime.totalMemory() / mb)

    //Print Maximum available memory
    val maxMemory = runtime.maxMemory() / mb
    println("Max Memory:" + runtime.maxMemory() / mb)

    Ok(Json.obj("TOTAL_MEMORY" -> totalMemorty, "USED_MEMORY" -> usedMemory, "FREE_MEMORY" -> freeMemory, "MAX_MEMORY" -> maxMemory))
  }
}

object Parser {
    val logger = LoggerFactory.getLogger(classOf[Application])
    def main(args:Array[String]) = {
      var template: String = "The amount of Rs.234 has been debited from your account XXXX2342. The balance in your account XXXX2342 is INR 43,008.22. The bal in your account no.xxxx2200 is Rs. 1139.29.";
      
      var tokens = template.split("\\s|(Rs.)|(INR)+");
      var acPattern = """[Xx]+[0-9]+(.?)""".r;
      var numPattern = """\d+""".r;
      
      //var amts: Array[Double];
      
      var ac = acPattern.findAllIn(template).toArray
      ac = for (a <- ac) yield a.replace(".", "");
      ac = for (a <- ac) yield a.replace(" ", "");
      ac = ac.distinct;
      logger.debug("Accounts >>> " + ac.toList);
      
      var amts = tokens.flatMap(s => Try( s.replaceAll(",", "").split("\\W+").mkString(".").toDouble ).toOption)

      logger.debug("Balances >>> " + amts.toList);

      
  }
}

object Tabulator {
  val logger = LoggerFactory.getLogger(classOf[Application])
  def format(table: Seq[Seq[Any]]) = table match {
    case Seq() => ""
    case _ => 
      val sizes = for (row <- table) yield (for (cell <- row) yield if (cell == null) 0 else cell.toString.length)
      val colSizes = for (col <- sizes.transpose) yield col.max
      val rows = for (row <- table) yield formatRow(row, colSizes)
      formatRows(rowSeparator(colSizes), rows)
  }

  def formatRows(rowSeparator: String, rows: Seq[String]): String = (
    rowSeparator :: 
    rows.head :: 
    rowSeparator :: 
    rows.tail.toList ::: 
    rowSeparator :: 
    List()).mkString("\n")

  def formatRow(row: Seq[Any], colSizes: Seq[Int]) = {
    val cells = (for ((item, size) <- row.zip(colSizes)) yield if (size == 0) "" else ("%" + size + "s").format(item))
    cells.mkString("|", "|", "|")
  }

  def rowSeparator(colSizes: Seq[Int]) = colSizes map { "-" * _ } mkString("+", "+", "+")
}
