/*
 * Copyright 2023 HM Revenue & Customs
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

import connectors.GetOtherEmploymentsIncomeConnector
import connectors.parsers.GetOtherEmploymentsIncomeParser.GetOtherEmploymentsIncomeResponse
import models._
import testUtils.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class GetOtherEmploymentsIncomeServiceSpec extends TestSuite {

  val connector: GetOtherEmploymentsIncomeConnector = mock[GetOtherEmploymentsIncomeConnector]
  val service: GetOtherEmploymentsIncomeService = new GetOtherEmploymentsIncomeService(connector)

  ".getOtherEmploymentsIncome" should {

    "return the connector response" in {

      val expectedResult: GetOtherEmploymentsIncomeResponse = Right(OtherEmploymentsIncomeModel(
        submittedOn = "2019-08-24T14:15:22Z",
        shareOption = Some(Seq(ShareOptionModel("string", Some("123/abc 001<Q>"), "EMI", "2019-08-24", "2019-08-24", Some(true),
          99999999999.99, 0, Some("string"), 99999999999.99, 99999999999.99, 99999999999.99, 99999999999.99,
          99999999999.99, 99999999999.99))),
        sharesAwardedOrReceived = Some(Seq(SharesAwardedOrReceivedModel("string", Some("123/abc 001<Q>"), "SIP", "2019-08-24", 0, "string",
          "2019-08-24", true, true, 99999999999.99, 99999999999.99, 99999999999.99,
          99999999999.99, 99999999999.99))),
        disability = Some(DisabilityModel(Some("string"), 99999999999.99)),
        foreignService = Some(ForeignServiceModel(Some("string"), 99999999999.99)),
        lumpSums = Some(Seq(LumpSumsModel("string", "123/abc 001<Q>",
          Some(TaxableLumpSumsAndCertainIncomeModel(99999999999.99, Some(99999999999.99), Some(true))),
          Some(BenefitFromEmployerFinancedRetirementSchemeModel(99999999999.99, Some(99999999999.99), Some(99999999999.99), Some(true))),
          Some(RedundancyCompensationPaymentsOverExemptionModel(99999999999.99, Some(99999999999.99), Some(true))),
          Some(RedundancyCompensationPaymentsUnderExemptionModel(99999999999.99)))))
      ))

      (connector.getOtherEmploymentsIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects("12345678", 1234, *)
        .returning(Future.successful(expectedResult))

      val result = await(service.getOtherEmploymentsIncome("12345678", 1234))

      result mustBe expectedResult

    }
  }
}
