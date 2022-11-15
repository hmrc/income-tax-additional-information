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

package controllers

import connectors.parsers.DeleteInsurancePoliciesParser.DeleteInsurancePoliciesResponse
import models.{ErrorBodyModel, ErrorModel}
import org.scalamock.handlers.CallHandler3
import play.api.http.Status._
import services.DeleteInsurancePoliciesService
import testUtils.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class DeleteinsurancePoliciesControllerSpec extends TestSuite{
  val serviceMock: DeleteInsurancePoliciesService = mock[DeleteInsurancePoliciesService]

  val controller = new DeleteInsurancePoliciesController(serviceMock, mockControllerComponents, authorisedAction)

  val notFoundModel: ErrorModel = ErrorModel(NOT_FOUND, ErrorBodyModel("NotFound", "Unable to find source"))
  val serviceUnavailableModel: ErrorModel =
    ErrorModel(SERVICE_UNAVAILABLE, ErrorBodyModel("SERVICE_UNAVAILABLE", "The service is currently unavailable"))
  val badRequestModel: ErrorModel = ErrorModel(BAD_REQUEST, ErrorBodyModel("BAD_REQUEST", "The supplied NINO is invalid"))
  val internalServerErrorModel: ErrorModel =
    ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel("INTERNAL_SERVER_ERROR", "There has been an unexpected error"))

  val nino = "nino"
  val taxYear = 2021

  "DeleteInsurancePoliciesData" should {

    "Return a NO CONTENT if deletes insurance policies data successful" in {

      val serviceResult = Right(true)

      def serviceCallMock(): CallHandler3[String, Int, HeaderCarrier, Future[DeleteInsurancePoliciesResponse] ]=
        (serviceMock.deleteInsurancePoliciesData(_: String, _: Int)(_: HeaderCarrier))
          .expects(nino, taxYear, *)
          .returning(Future.successful(serviceResult))

      val result = {
        mockAuth()
        serviceCallMock()
        controller.deleteInsurancePoliciesData(nino, taxYear)(fakeRequest)
      }

      status(result) mustBe NO_CONTENT

    }

    "return a Left response" when {

      def mockDeleteSavingIncomeDataWithError(errorModel: ErrorModel): CallHandler3[String, Int, HeaderCarrier, Future[DeleteInsurancePoliciesResponse]] = {
        (serviceMock.deleteInsurancePoliciesData(_: String, _: Int)(_: HeaderCarrier))
          .expects(nino, taxYear, *)
          .returning(Future.successful(Left(errorModel)))
      }

      "the service returns a NO_CONTENT" in {
        val result = {
          mockAuth()
          mockDeleteSavingIncomeDataWithError(notFoundModel)
          controller.deleteInsurancePoliciesData(nino, taxYear)(fakeRequest)
        }
        status(result) mustBe NOT_FOUND
      }

      "the service returns a SERVICE_UNAVAILABLE" in {
        val result = {
          mockAuth()
          mockDeleteSavingIncomeDataWithError(serviceUnavailableModel)
          controller.deleteInsurancePoliciesData(nino, taxYear)(fakeRequest)
        }
        status(result) mustBe SERVICE_UNAVAILABLE
      }
      "the service returns a BAD_REQUEST" in {
        val result = {
          mockAuth()
          mockDeleteSavingIncomeDataWithError(badRequestModel)
          controller.deleteInsurancePoliciesData(nino, taxYear)(fakeRequest)
        }
        status(result) mustBe BAD_REQUEST
      }
      "the service returns a INTERNAL_SERVER_ERROR" in {
        val result = {
          mockAuth()
          mockDeleteSavingIncomeDataWithError(internalServerErrorModel)
          controller.deleteInsurancePoliciesData(nino, taxYear)(fakeRequest)
        }
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
