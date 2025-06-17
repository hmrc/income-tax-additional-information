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

import play.api.libs.json.{JsObject, Json}
import testUtils.TestSuite

class CreateOrUpdateOtherReliefsModelSpec extends TestSuite {

  val validNonDeductableLoanInterestModel: NonDeductableLoanInterestModel = NonDeductableLoanInterestModel(
    customerReference = Some("RefNo13254687"),
    reliefClaimed = 123.45
  )

  val validPayrollGivingModel: PayrollGivingModel = PayrollGivingModel(
    customerReference = Some("RefNo13254687"),
    reliefClaimed = 123.46
  )

  val validQualifyingDistributionModel: QualifyingDistributionModel = QualifyingDistributionModel(
    customerReference = Some("RefNo13254687"),
    amount = 123.47
  )

  val validMaintenancePaymentsModel: MaintenancePaymentsModel = MaintenancePaymentsModel(
    customerReference = Some("RefNo13254687"),
    exSpouseName = Some("Jane Doe"),
    exSpouseDateOfBirth = Some("01-01-2000"),
    amount = 123.48
  )

  val validPostCessationTradeReliefModel: PostCessationTradeReliefModel = PostCessationTradeReliefModel(
    customerReference = Some("RefNo13254687"),
    businessName = Some("Monsters Inc"),
    dateBusinessCeased = Some("01-01-2025"),
    natureOfTrade = Some("Power Distribution"),
    incomeSource = Some("Cash"),
    amount = 123.49
  )

  val validAnnualPaymentsMadeModel: AnnualPaymentsMadeModel = AnnualPaymentsMadeModel(
    customerReference = Some("RefNo13254687"),
    reliefClaimed = 123.50
  )

  val validQualifyingLoanInterestPaymentsModel: QualifyingLoanInterestPaymentsModel = QualifyingLoanInterestPaymentsModel(
    customerReference = Some("RefNo13254687"),
    lenderName = Some("Bank"),
    reliefClaimed = 123.51
  )

  val validCreateOrUpdateOtherReliefsModel: CreateOrUpdateOtherReliefsModel = CreateOrUpdateOtherReliefsModel(
    Some(validNonDeductableLoanInterestModel),
    Some(validPayrollGivingModel),
    Some(validQualifyingDistributionModel),
    Some(Seq(validMaintenancePaymentsModel)),
    Some(Seq(validPostCessationTradeReliefModel)),
    Some(validAnnualPaymentsMadeModel),
    Some(Seq(validQualifyingLoanInterestPaymentsModel))
  )

  val emptyCreateOrUpdateOtherReliefsModel: CreateOrUpdateOtherReliefsModel = CreateOrUpdateOtherReliefsModel(
    None,
    None,
    None,
    None,
    None,
    None,
    None
  )

  val emptySeqCreateOrUpdateOtherReliefsModel: CreateOrUpdateOtherReliefsModel = CreateOrUpdateOtherReliefsModel(
    None,
    None,
    None,
    Some(Seq.empty),
    Some(Seq.empty),
    None,
    Some(Seq.empty)
  )

  val validJson: JsObject = Json.obj(
    "nonDeductableLoanInterest" -> Json.obj(
      "customerReference" -> "RefNo13254687",
      "reliefClaimed" -> 123.45
    ),
    "payrollGiving" -> Json.obj(
      "customerReference" -> "RefNo13254687",
      "reliefClaimed" -> 123.46
    ),
    "qualifyingDistributionRedemptionOfSharesAndSecurities" -> Json.obj(
      "customerReference" -> "RefNo13254687",
      "amount" -> 123.47
    ),
    "maintenancePayments" -> Json.arr(
      Json.obj(
        "customerReference" -> "RefNo13254687",
        "exSpouseName" -> "Jane Doe",
        "exSpouseDateOfBirth" -> "01-01-2000",
        "amount" -> 123.48
      )
    ),
    "postCessationTradeReliefAndCertainOtherLosses" -> Json.arr(
      Json.obj(
        "customerReference" -> "RefNo13254687",
        "businessName" -> "Monsters Inc",
        "dateBusinessCeased" -> "01-01-2025",
        "natureOfTrade" -> "Power Distribution",
        "incomeSource" -> "Cash",
        "amount" -> 123.49
      )
    ),
    "annualPaymentsMade" -> Json.obj(
      "customerReference" -> "RefNo13254687",
      "reliefClaimed" -> 123.50
    ),
    "qualifyingLoanInterestPayments" -> Json.arr(
      Json.obj(
        "customerReference" -> "RefNo13254687",
        "lenderName" -> "Bank",
        "reliefClaimed" -> 123.51
      )
    ),
  )

  val emptyJson: JsObject = Json.obj()

  "CreateOrUpdateOtherReliefs" should {
    "parse from json for valid" in {
      validJson.as[CreateOrUpdateOtherReliefsModel](CreateOrUpdateOtherReliefsModel.reads) mustBe validCreateOrUpdateOtherReliefsModel
    }
    "parse from json for empty json" in {
      emptyJson.as[CreateOrUpdateOtherReliefsModel](CreateOrUpdateOtherReliefsModel.reads) mustBe emptyCreateOrUpdateOtherReliefsModel
    }

    "parse to json for valid" in {
      Json.toJson(validCreateOrUpdateOtherReliefsModel)(CreateOrUpdateOtherReliefsModel.writes) mustBe validJson
    }
    "parse to json for full empty model" in {
      Json.toJson(emptyCreateOrUpdateOtherReliefsModel)(CreateOrUpdateOtherReliefsModel.writes) mustBe emptyJson
    }
    "parse to json for model with empty seq()" in {
      Json.toJson(emptySeqCreateOrUpdateOtherReliefsModel)(CreateOrUpdateOtherReliefsModel.writes) mustBe emptyJson
    }
  }

}
