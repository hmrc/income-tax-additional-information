

package models

import play.api.libs.json.{Json, OFormat}

case class InsurancePoliciesModel(submittedOn: String,
                                  lifeInsurance: Seq[LifeInsuranceModel],
                                  capitalRedemption: Seq[CapitalRedemptionModel],
                                  lifeAnnuity: Seq[LifeAnnuityModel],
                                  voidedIsa: Seq[VoidedIsaModel],
                                  foreign: Seq[ForeignModel])

object InsurancePoliciesModel {
  implicit val formats: OFormat[InsurancePoliciesModel] = Json.format[InsurancePoliciesModel]
}