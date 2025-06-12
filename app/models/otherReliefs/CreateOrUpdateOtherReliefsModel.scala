/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.otherReliefs

import play.api.libs.json.{Json, OWrites, Reads}

case class CreateOrUpdateOtherReliefsModel(nonDeductableLoanInterest: Option[NonDeductableLoanInterestModel],
                                           payrollGiving: Option[PayrollGivingModel],
                                           qualifyingDistributionRedemptionOfSharesAndSecurities: Option[QualifyingDistributionModel],
                                           maintenancePayments: Option[Seq[MaintenancePaymentsModel]],
                                           postCessationTradeReliefAndCertainOtherLosses: Option[Seq[PostCessationTradeReliefModel]],
                                           annualPaymentsMade: Option[AnnualPaymentsMadeModel],
                                           qualifyingLoanInterestPayments: Option[Seq[QualifyingLoanInterestPaymentsModel]])

object CreateOrUpdateOtherReliefsModel {
  implicit val reads: Reads[CreateOrUpdateOtherReliefsModel] = Json.reads[CreateOrUpdateOtherReliefsModel]

  implicit val writes: OWrites[CreateOrUpdateOtherReliefsModel] = OWrites[CreateOrUpdateOtherReliefsModel] { model =>
    val removedEmpty = model.copy(
      maintenancePayments = if (model.maintenancePayments.exists(_.isEmpty)) None else model.maintenancePayments,
      postCessationTradeReliefAndCertainOtherLosses = if (model.postCessationTradeReliefAndCertainOtherLosses.exists(_.isEmpty)) None else model.postCessationTradeReliefAndCertainOtherLosses,
      qualifyingLoanInterestPayments = if (model.qualifyingLoanInterestPayments.exists(_.isEmpty)) None else model.qualifyingLoanInterestPayments
    )

    Json.writes[CreateOrUpdateOtherReliefsModel].writes(removedEmpty)
  }
}
