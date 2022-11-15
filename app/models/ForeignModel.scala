

package models

import play.api.libs.json.{Json, OFormat}

case class ForeignModel(
                         customerReference: Option[String],
                         gainAmount: BigDecimal,
                         taxPaidAmount: Option[BigDecimal],
                         yearsHeld: Option[Int]
                       )

object ForeignModel {
  implicit val formats: OFormat[ForeignModel] = Json.format[ForeignModel]
}