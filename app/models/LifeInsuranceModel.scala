

package models

import play.api.libs.json.{Json, OFormat}

case class LifeInsuranceModel(
                               customerReference: Option[String],
                               event: Option[String],
                               gainAmount: BigDecimal,
                               taxPaid: Option[Boolean],
                               yearsHeld: Option[Int],
                               yearsHeldSinceLastGain: Option[Int],
                               deficiencyRelief: Option[BigDecimal]
                             )

object LifeInsuranceModel {
  implicit val formats: OFormat[LifeInsuranceModel] = Json.format[LifeInsuranceModel]
}