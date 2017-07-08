package helpers

import javax.inject.Inject

import org.slf4j.LoggerFactory
import play.Configuration
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.ws.WSClient

import scala.io.Source

class SolrIndexHelper @Inject() (implicit ws: WSClient, config: Configuration) {

  val logger, log = LoggerFactory.getLogger(classOf[SolrIndexHelper])

  def addDocument(path: String) {

    val lines = Source.fromFile(path).getLines.toList
    var data: Seq[JsObject] = Seq.empty
    lines.foreach { line =>

      val tokens = line.split("\\,")

      data = data :+ (
        Json.obj("fid" -> tokens.apply(0),
          "dname" -> tokens.apply(1),
          "dage" -> tokens.apply(2),
          "drat" -> tokens.apply(3),
          "daum" -> tokens.apply(4),
          "damc" -> tokens.apply(5),
          "cid" -> tokens.apply(6),
          "dcat" -> tokens.apply(7),
          "scid" -> tokens.apply(8),
          "dscat" -> tokens.apply(9),
          "drisk" -> tokens.apply(10),
          "drt1m" -> tokens.apply(11),
          "drt3m" -> tokens.apply(12),
          "drt6m" -> tokens.apply(13),
          "drt1y" -> tokens.apply(14),
          "drt3y" -> tokens.apply(15),
          "drt5y" -> tokens.apply(16),
          "drtsi" -> tokens.apply(17)));

      if (data.length == 5) {
        logger.debug(data.toString())
        val abc = JsArray.apply(data)
        ws.url("http://10.1.0.2:8983/solr/funds/update/json/docs?commit=true")
          .withHeaders(("Content-Type" -> "application/json"))
          .post(abc)

        data = Seq.empty
      }
    }

  }

  def addIds() {
    var lines = Source.fromFile("D://bank list.csv").getLines.toList
    var data: Seq[JsObject] = Seq.empty

    lines.foreach { line =>
      val tokens = line.split("\\,")
      data = data :+ (
        Json.obj("bid" -> tokens.apply(0),
          "db" -> tokens.apply(1)))
      if (data.length == 3) {
        logger.debug(data.toString)
        val abc = JsArray.apply(data)
        ws.url("http://10.1.0.2:8983/solr/banks/update/json/docs?commit=true")
          .withHeaders(("Content-Type" -> "application/json"))
          .post(abc)

        data = Seq.empty
      }
    }
  }

  def addBankDocuments(path: String) {
    val lines = Source.fromFile(path).getLines.toList
    var data: Seq[JsObject] = Seq.empty
    var bankName = "";
    var nameList: Seq[String] = Seq.empty;
    var idCount = 0;
    lines.foreach { line =>

      val tokens = line.split("\\,")

      if (!bankName.equals(tokens.apply(0))) {
        bankName = tokens.apply(0);
        nameList = nameList :+ bankName;
        idCount = idCount + 1;
      }

      data = data :+ (
        Json.obj("bid" -> idCount,
          // "db" -> tokens.apply(0),
          "dIFSC" -> tokens.apply(1),
          "dbr" -> tokens.apply(3),
          // "daddr" -> tokens.apply(4),
          "dcity" -> tokens.apply(5)))

      if (data.length == 500) {
        logger.debug(data.toString)
        val abc = JsArray.apply(data)
        ws.url("http://10.1.0.2:8983/solr/banks/update/json/docs?commit=true")
          .withHeaders(("Content-Type" -> "application/json"))
          .post(abc)

        data = Seq.empty
      }

    }

    if (data.length > 0) {
      logger.debug(data.toString)
      val abc = JsArray.apply(data)
      ws.url("http://10.1.0.2:8983/solr/banks/update/json/docs?commit=true")
        .withHeaders(("Content-Type" -> "application/json"))
        .post(abc)

      data = Seq.empty
    }
    nameList.foreach { (name) => logger.debug(name); }
  }
}