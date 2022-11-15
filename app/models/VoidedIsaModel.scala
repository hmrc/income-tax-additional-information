

package models

import play.api.libs.json.{Json, OFormat}

case class VoidedIsaModel(
                           customerReference: Option[String],
                           event: Option[String],
                           gainAmount: BigDecimal,
                           taxPaidAmount: Option[BigDecimal],
                           yearsHeld: Option[Int],
                           yearsHeldSinceLastGain: Option[Int]
                         )

object VoidedIsaModel {
  implicit val formats: OFormat[VoidedIsaModel] = Json.format[VoidedIsaModel]
}