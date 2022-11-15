

package models

import play.api.libs.json.{Json, OFormat}

case class CapitalRedemptionModel(
                                   customerReference: Option[String],
                                   event: Option[String],
                                   gainAmount: BigDecimal,
                                   taxPaid: Option[Boolean],
                                   yearsHeld: Option[Int],
                                   yearsHeldSinceLastGain: Option[Int],
                                   deficiencyRelief: Option[BigDecimal]
                                 )

object CapitalRedemptionModel {
  implicit val formats: OFormat[CapitalRedemptionModel] = Json.format[CapitalRedemptionModel]
}