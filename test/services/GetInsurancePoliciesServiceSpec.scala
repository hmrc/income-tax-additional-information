/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.GetInsurancePoliciesConnector
import connectors.parsers.GetInsurancePoliciesParser.GetInsurancePoliciesResponse
import models._
import testUtils.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class GetInsurancePoliciesServiceSpec extends TestSuite {

  val connector: GetInsurancePoliciesConnector = mock[GetInsurancePoliciesConnector]
  val service: GetInsurancePoliciesService = new GetInsurancePoliciesService(connector)

  ".getInsurancePolicies" should {

    "return the connector response" in {

      val expectedResult: GetInsurancePoliciesResponse = Right(InsurancePoliciesModel(
        submittedOn = "2020-01-04T05:01:01Z",
        lifeInsurance = Seq(LifeInsuranceModel(Some("RefNo13254687"), Some("Life"), 123.45, Some(true), Some(4), Some(3), Some(123.45))),
        capitalRedemption = Some(Seq(CapitalRedemptionModel(Some("RefNo13254687"), Some("Capital"), 123.45, Some(true), Some(3), Some(2), Some(0)))),
        lifeAnnuity = Some(Seq(LifeAnnuityModel(Some("RefNo13254687"), Some("Life"), 0, Some(true), Some(2), Some(22), Some(123.45)))),
        voidedIsa = Some(Seq(VoidedIsaModel(Some("RefNo13254687"), Some("isa"), 123.45, Some(123.45), Some(5), Some(6)))),
        foreign = Some(Seq(ForeignModel(Some("RefNo13254687"), 123.45, Some(123.45), Some(3))))
      ))

      (connector.getInsurancePolicies(_: String, _: Int)(_: HeaderCarrier))
        .expects("12345678", 1234, *)
        .returning(Future.successful(expectedResult))

      val result = await(service.getInsurancePolicies("12345678", 1234))

      result mustBe expectedResult

    }
  }
}
