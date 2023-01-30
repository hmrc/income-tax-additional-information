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

package controllers

import connectors.parsers.GetOtherEmploymentsIncomeParser.GetOtherEmploymentsIncomeResponse
import models.{OtherEmploymentsIncomeModel, _}
import org.scalamock.handlers.CallHandler3
import play.api.http.Status._
import play.api.libs.json.Json
import services.GetOtherEmploymentsIncomeService
import testUtils.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class GetOtherEmploymentsIncomeControllerSpec extends TestSuite {

  val serviceMock: GetOtherEmploymentsIncomeService = mock[GetOtherEmploymentsIncomeService]
  val controller = new GetOtherEmploymentsIncomeController(serviceMock, mockControllerComponents, authorisedAction)

  val notFoundModel: ErrorModel = ErrorModel(NOT_FOUND, ErrorBodyModel("NotFound", "Unable to find source"))
  val serviceUnavailableModel: ErrorModel =
    ErrorModel(SERVICE_UNAVAILABLE, ErrorBodyModel("SERVICE_UNAVAILABLE", "The service is currently unavailable"))
  val badRequestModel: ErrorModel = ErrorModel(BAD_REQUEST, ErrorBodyModel("BAD_REQUEST", "The supplied NINO is invalid"))
  val unprocessableEntityModel: ErrorModel = ErrorModel(UNPROCESSABLE_ENTITY, ErrorBodyModel("UNPROCESSABLE_ENTITY", "Tax year is is not supported"))
  val internalServerErrorModel: ErrorModel =
    ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel("INTERNAL_SERVER_ERROR", "There has been an unexpected error"))


  val nino = "nino"
  val taxYear = 2024
  val mtditid = "someMtditid"

  val otherEmploymentsIncomeModel: OtherEmploymentsIncomeModel = OtherEmploymentsIncomeModel(
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
  )

  ".getOtherEmploymentsIncome" should {

    "Return a 200 OK response with valid other employments income" in {

      val serviceResult = Right(otherEmploymentsIncomeModel)
      val finalResult = Json.toJson(otherEmploymentsIncomeModel).toString()

      def serviceCallMock(): CallHandler3[String, Int, HeaderCarrier, Future[GetOtherEmploymentsIncomeResponse]] =
        (serviceMock.getOtherEmploymentsIncome(_: String, _: Int)(_: HeaderCarrier))
          .expects(nino, taxYear, *)
          .returning(Future.successful(serviceResult))


      val result = {
        mockAuth()
        serviceCallMock()
        controller.getOtherEmploymentsIncome(nino, taxYear)(fakeRequest)
      }

      status(result) mustBe OK
      bodyOf(result) mustBe finalResult

    }

    "return a Left response" when {

      def mockGetOtherEmploymentsIncomeDataWithError(errorModel: ErrorModel): CallHandler3[String, Int, HeaderCarrier, Future[GetOtherEmploymentsIncomeResponse]] = {
        (serviceMock.getOtherEmploymentsIncome(_: String, _: Int)(_: HeaderCarrier))
          .expects(nino, taxYear, *)
          .returning(Future.successful(Left(errorModel)))
      }

      "the service returns a NO_CONTENT" in {
        val result = {
          mockAuth()
          mockGetOtherEmploymentsIncomeDataWithError(notFoundModel)
          controller.getOtherEmploymentsIncome(nino, taxYear)(fakeRequest)
        }
        status(result) mustBe NOT_FOUND
      }

      "the service returns a SERVICE_UNAVAILABLE" in {
        val result = {
          mockAuth()
          mockGetOtherEmploymentsIncomeDataWithError(serviceUnavailableModel)
          controller.getOtherEmploymentsIncome(nino, taxYear)(fakeRequest)
        }
        status(result) mustBe SERVICE_UNAVAILABLE
      }
      "the service returns a BAD_REQUEST" in {
        val result = {
          mockAuth()
          mockGetOtherEmploymentsIncomeDataWithError(badRequestModel)
          controller.getOtherEmploymentsIncome(nino, taxYear)(fakeRequest)
        }
        status(result) mustBe BAD_REQUEST
      }
      "the service returns a UNPROCESSABLE_ENTITY" in {
        val result = {
          mockAuth()
          mockGetOtherEmploymentsIncomeDataWithError(unprocessableEntityModel)
          controller.getOtherEmploymentsIncome(nino, taxYear)(fakeRequest)
        }
        status(result) mustBe UNPROCESSABLE_ENTITY
      }
      "the service returns a INTERNAL_SERVER_ERROR" in {
        val result = {
          mockAuth()
          mockGetOtherEmploymentsIncomeDataWithError(internalServerErrorModel)
          controller.getOtherEmploymentsIncome(nino, taxYear)(fakeRequest)
        }
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
