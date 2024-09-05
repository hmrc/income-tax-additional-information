/*
 * Copyright 2024 HM Revenue & Customs
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

package services

import connectors.parsers.GetInsurancePoliciesParser.GetInsurancePoliciesResponse
import models._
import models.tasklist.{SectionTitle, TaskListSection, TaskListSectionItem, TaskStatus, TaskTitle}
import play.api.http.Status.NOT_FOUND
import support.providers.AppConfigStubProvider
import testUtils.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class CommonTaskListServiceSpec extends TestSuite with AppConfigStubProvider {

  val gainsService: GetInsurancePoliciesService = mock[GetInsurancePoliciesService]

  val service: CommonTaskListService = new CommonTaskListService(appConfigStub, gainsService)

  val ZERO: BigDecimal = 0
  val nino: String = "12345678"
  val taxYear: Int = 1234
  val monetaryValue: BigDecimal = 123.45
  val years: Int = 4

  val fullGainsResult: GetInsurancePoliciesResponse = Right(InsurancePoliciesModel(
    submittedOn = "2020-01-04T05:01:01Z",
    lifeInsurance =
      Some(Seq(LifeInsuranceModel(Some("RefNo13254687"), Some("Life"), monetaryValue, Some(true), Some(years), Some(years), Some(monetaryValue)))),
    capitalRedemption =
      Some(Seq(CapitalRedemptionModel(Some("RefNo13254687"), Some("Capital"), monetaryValue, Some(true), Some(years), Some(years), Some(ZERO)))),
    lifeAnnuity = Some(Seq(LifeAnnuityModel(Some("RefNo13254687"), Some("Life"), ZERO, Some(true), Some(years), Some(years), Some(monetaryValue)))),
    voidedIsa = Some(Seq(VoidedIsaModel(Some("RefNo13254687"), Some("isa"), monetaryValue, Some(monetaryValue), Some(years), Some(years))))
  ))

  val emptyGainsResult: GetInsurancePoliciesResponse = Left(ErrorModel(NOT_FOUND, ErrorBodyModel("SOME_CODE", "reason")))

  val fullTaskSection: TaskListSection =
    TaskListSection(SectionTitle.InsuranceGainsTitle,
      Some(List(
        TaskListSectionItem(TaskTitle.LifeInsurance, TaskStatus.Completed,
          Some("http://localhost:10007/update-and-submit-income-tax-return/additional-information/1234/gains/summary?policyType=Life+Insurance")
        ),
        TaskListSectionItem(TaskTitle.LifeAnnuity, TaskStatus.Completed,
          Some("http://localhost:10007/update-and-submit-income-tax-return/additional-information/1234/gains/summary?policyType=Life+Annuity")
        ),
        TaskListSectionItem(TaskTitle.CapitalRedemption, TaskStatus.Completed,
          Some("http://localhost:10007/update-and-submit-income-tax-return/additional-information/1234/gains/summary?policyType=Capital+Redemption")
        ),
        TaskListSectionItem(TaskTitle.VoidedISA, TaskStatus.Completed,
          Some("http://localhost:10007/update-and-submit-income-tax-return/additional-information/1234/gains/summary?policyType=Voided+ISA")
        )
      ))
    )

  "CommonTaskListService.get" should {

    "return a full task list section model" in {

      (gainsService.getInsurancePolicies(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(fullGainsResult))

      val underTest = service.get(taxYear, nino)

      await(underTest) mustBe fullTaskSection
    }

    "return a minimal task list section model" in {

      (gainsService.getInsurancePolicies(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(
          InsurancePoliciesModel("", lifeInsurance = Some(Seq(LifeInsuranceModel(Some("CR"), None, monetaryValue, Some(false), None, None, None))))
        )))

      val underTest = service.get(taxYear, nino)

      await(underTest) mustBe fullTaskSection.copy(
        taskItems = Some(List(
          TaskListSectionItem(TaskTitle.LifeInsurance, TaskStatus.Completed,
            Some("http://localhost:10007/update-and-submit-income-tax-return/additional-information/1234/gains/summary?policyType=Life+Insurance")
          )
        ))
      )
    }

    "return an empty task list section model" in {

      (gainsService.getInsurancePolicies(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(emptyGainsResult))

      val underTest = service.get(taxYear, nino)

      await(underTest) mustBe TaskListSection(SectionTitle.InsuranceGainsTitle, None)
    }
  }
}
