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

import connectors.parsers.DeleteOtherEmploymentsIncomeParser.DeleteOtherEmploymentsIncomeResponse
import models.{ErrorBodyModel, ErrorModel}
import org.scalamock.handlers.CallHandler3
import play.api.http.Status._
import services.DeleteOtherEmploymentsIncomeService
import testUtils.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class DeleteOtherEmploymentsIncomeControllerSpec extends TestSuite {
  val serviceMock: DeleteOtherEmploymentsIncomeService = mock[DeleteOtherEmploymentsIncomeService]

  val controller = new DeleteOtherEmploymentsIncomeController(serviceMock, mockControllerComponents, authorisedAction)

  val notFoundModel: ErrorModel = ErrorModel(NOT_FOUND, ErrorBodyModel("NotFound", "Unable to find source"))
  val serviceUnavailableModel: ErrorModel =
    ErrorModel(SERVICE_UNAVAILABLE, ErrorBodyModel("SERVICE_UNAVAILABLE", "The service is currently unavailable"))
  val badRequestModel: ErrorModel = ErrorModel(BAD_REQUEST, ErrorBodyModel("BAD_REQUEST", "The supplied NINO is invalid"))
  val internalServerErrorModel: ErrorModel =
    ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel("INTERNAL_SERVER_ERROR", "There has been an unexpected error"))

  val nino = "nino"
  val taxYear = 2021

  "DeleteOtherEmploymentsIncome" should {

    "Return a NO CONTENT if deletes other employments income data successful" in {

      val serviceResult = Right(())

      def serviceCallMock(): CallHandler3[String, Int, HeaderCarrier, Future[DeleteOtherEmploymentsIncomeResponse]] =
        (serviceMock.deleteOtherEmploymentsIncomeData(_: String, _: Int)(_: HeaderCarrier))
          .expects(nino, taxYear, *)
          .returning(Future.successful(serviceResult))

      val result = {
        mockAuth()
        serviceCallMock()
        controller.deleteOtherEmploymentsIncomeData(nino, taxYear)(fakeRequest)
      }

      status(result) mustBe NO_CONTENT

    }

    "return a Left response" when {

      def mockDeleteOtherEmploymentsIncomeDataWithError(errorModel: ErrorModel): CallHandler3[String,
        Int, HeaderCarrier, Future[DeleteOtherEmploymentsIncomeResponse]] = {
        (serviceMock.deleteOtherEmploymentsIncomeData(_: String, _: Int)(_: HeaderCarrier))
          .expects(nino, taxYear, *)
          .returning(Future.successful(Left(errorModel)))
      }

      "the service returns a NO_CONTENT" in {
        val result = {
          mockAuth()
          mockDeleteOtherEmploymentsIncomeDataWithError(notFoundModel)
          controller.deleteOtherEmploymentsIncomeData(nino, taxYear)(fakeRequest)
        }
        status(result) mustBe NOT_FOUND
      }

      "the service returns a SERVICE_UNAVAILABLE" in {
        val result = {
          mockAuth()
          mockDeleteOtherEmploymentsIncomeDataWithError(serviceUnavailableModel)
          controller.deleteOtherEmploymentsIncomeData(nino, taxYear)(fakeRequest)
        }
        status(result) mustBe SERVICE_UNAVAILABLE
      }
      "the service returns a BAD_REQUEST" in {
        val result = {
          mockAuth()
          mockDeleteOtherEmploymentsIncomeDataWithError(badRequestModel)
          controller.deleteOtherEmploymentsIncomeData(nino, taxYear)(fakeRequest)
        }
        status(result) mustBe BAD_REQUEST
      }
      "the service returns a INTERNAL_SERVER_ERROR" in {
        val result = {
          mockAuth()
          mockDeleteOtherEmploymentsIncomeDataWithError(internalServerErrorModel)
          controller.deleteOtherEmploymentsIncomeData(nino, taxYear)(fakeRequest)
        }
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
