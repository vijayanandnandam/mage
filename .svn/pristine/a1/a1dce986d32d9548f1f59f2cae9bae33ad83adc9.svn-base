package models

case class SchemeModel(scheme_id: Int, scheme_name: String, amc: String, category: String, sub_category: String, 
    cap_type: String, inception: String, sip_facility: String, risk: Int, aum: Option[Double], min_inv: Double, exit_load: String, 
    min_inv_sip: Option[Double], nav: Option[Double], return_max: Option[Double], return_1m: Option[Double], return_3m: Option[Double], 
    return_6m: Option[Double], return_1yr: Option[Double], return_3yr: Option[Double], return_5yr: Option[Double])

object SchemeModel{
  
  import play.api.libs.json.Json
  implicit val schemeFormat = Json.format[SchemeModel]
}