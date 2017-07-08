package models


case class CNDDoc(cndRfnum: Long,
                  cndName: String,
                  cndDescription: String,
                  cndGroupName: String,
                  cndActiveFlag: String,
                  cndCndRfnum: Long,
                  cndExternalField1: String,
                  cndExternalField2: String,
                  cndExternalField3: String,
                  cndExternalField4: String,
                  cndCode: String,
                  cndSequence: Double)


object CNDJsonFormats {

  import play.api.libs.json.Json

  implicit val cNDDocFormat = Json.format[CNDDoc]

}
