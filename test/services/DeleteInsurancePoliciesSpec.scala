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

import connectors.{DeleteInsurancePoliciesConnector, DeleteInsurancePoliciesTysConnector}
import connectors.parsers.DeleteInsurancePoliciesParser.DeleteInsurancePoliciesResponse
import testUtils.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class DeleteInsurancePoliciesSpec extends TestSuite {
  val connector: DeleteInsurancePoliciesConnector = mock[DeleteInsurancePoliciesConnector]
  val tysConnector: DeleteInsurancePoliciesTysConnector = mock[DeleteInsurancePoliciesTysConnector]
  val service: DeleteInsurancePoliciesService = new DeleteInsurancePoliciesService(connector, tysConnector)

  "DeleteInsurancePoliciesConnector.deleteInsurancePolicies" should {

    "return the connector response" in {

      val expectedResult: DeleteInsurancePoliciesResponse = Right(true)

      (connector.deleteInsurancePoliciesData(_: String, _: Int)(_: HeaderCarrier))
        .expects("12345678", 1234, *)
        .returning(Future.successful(expectedResult))

      val result = await(service.deleteInsurancePoliciesData("12345678", 1234))

      result mustBe expectedResult

    }
  }

  "DeleteInsurancePoliciesTysConnector.deleteInsurancePolicies" should {

    "return the connector response" in {

      val expectedResult: DeleteInsurancePoliciesResponse = Right(true)

      (tysConnector.deleteInsurancePoliciesData(_: String, _: Int)(_: HeaderCarrier))
        .expects("12345678", 2024, *)
        .returning(Future.successful(expectedResult))

      val result = await(service.deleteInsurancePoliciesData("12345678", 2024))

      result mustBe expectedResult

    }
  }
}
