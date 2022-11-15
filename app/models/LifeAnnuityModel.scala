

package models

import play.api.libs.json.{Json, OFormat}

case class LifeAnnuityModel(
                             customerReference: Option[String],
                             event: Option[String],
                             gainAmount: BigDecimal,
                             taxPaid: Option[Boolean],
                             yearsHeld: Option[Int],
                             yearsHeldSinceLastGain: Option[Int],
                             deficiencyRelief: Option[BigDecimal]
                           )

object LifeAnnuityModel {
  implicit val formats: OFormat[LifeAnnuityModel] = Json.format[LifeAnnuityModel]
}