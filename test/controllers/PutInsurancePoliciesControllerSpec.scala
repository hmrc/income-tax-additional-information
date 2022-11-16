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

import connectors.parsers.PutInsurancePoliciesParser.PutInsurancePoliciesResponse
import models._
import org.scalamock.handlers.CallHandler4
import play.api.http.Status._
import play.api.libs.json.Json
import services.PutInsurancePoliciesService
import testUtils.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class PutInsurancePoliciesControllerSpec extends TestSuite {

  val serviceMock: PutInsurancePoliciesService = mock[PutInsurancePoliciesService]
  val controller = new PutInsurancePoliciesController(serviceMock, mockControllerComponents, authorisedAction)

  val notFoundModel: ErrorModel = ErrorModel(NOT_FOUND, ErrorBodyModel("NotFound", "Unable to find source"))
  val serviceUnavailableModel: ErrorModel =
    ErrorModel(SERVICE_UNAVAILABLE, ErrorBodyModel("SERVICE_UNAVAILABLE", "The service is currently unavailable"))
  val badRequestModel: ErrorModel = ErrorModel(BAD_REQUEST, ErrorBodyModel("BAD_REQUEST", "The supplied NINO is invalid"))
  val internalServerErrorModel: ErrorModel =
    ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel("INTERNAL_SERVER_ERROR", "There has been an unexpected error"))


  val nino = "nino"
  val taxYear = 2023
  val mtditid = "someMtditid"

  val model: CreateOrAmendInsurancePoliciesModel = CreateOrAmendInsurancePoliciesModel(
    lifeInsurance = Seq(LifeInsuranceModel(Some("RefNo13254687"), Some("Life"), 123.45, Some(true), Some(4), Some(3), Some(123.45))),
    capitalRedemption = Some(Seq(CapitalRedemptionModel(Some("RefNo13254687"), Some("Capital"), 123.45, Some(true), Some(3), Some(2), Some(0)))),
    lifeAnnuity = Some(Seq(LifeAnnuityModel(Some("RefNo13254687"), Some("Life"), 0, Some(true), Some(2), Some(22), Some(123.45)))),
    voidedIsa = Some(Seq(VoidedIsaModel(Some("RefNo13254687"), Some("isa"), 123.45, Some(123.45), Some(5), Some(6)))),
    foreign = Some(Seq(ForeignModel(Some("RefNo13254687"), 123.45, Some(123.45), Some(3))))
  )

  ".putInsurancePolicies" should {

    "Return a 204 NO Content response with valid CreateOrAmendSavings" in {

      val serviceResult = Right(true)
      val finalResult = Json.toJson(model).toString()

      def serviceCallMock(): CallHandler4[String, Int, CreateOrAmendInsurancePoliciesModel, HeaderCarrier, Future[PutInsurancePoliciesResponse]] =
        (serviceMock.putInsurancePolicies(_: String, _: Int, _: CreateOrAmendInsurancePoliciesModel)(_: HeaderCarrier))
          .expects(nino, taxYear, model, *)
          .returning(Future.successful(serviceResult))

      val result = {
        mockAuth()
        serviceCallMock()
        controller.putInsurancePolicies(nino, taxYear)(fakeRequestWithMtditid.withJsonBody(Json.toJson(model)))
      }
      status(result) mustBe NO_CONTENT
    }

    "return a Left response" when {

      def mockCreateOrAmendSavingsWithError(errorModel: ErrorModel): CallHandler4[String, Int, CreateOrAmendInsurancePoliciesModel, HeaderCarrier, Future[PutInsurancePoliciesResponse]] = {
        (serviceMock.putInsurancePolicies(_: String, _: Int, _: CreateOrAmendInsurancePoliciesModel)(_: HeaderCarrier))
          .expects(nino, taxYear, *, *)
          .returning(Future.successful(Left(errorModel)))
      }

      "the service returns a NO_CONTENT" in {
        val result = {
          mockAuth()
          mockCreateOrAmendSavingsWithError(notFoundModel)
          controller.putInsurancePolicies(nino, taxYear)(fakeRequestWithMtditid.withJsonBody(Json.toJson(model)))
        }
        status(result) mustBe NOT_FOUND
      }

      "the service returns a SERVICE_UNAVAILABLE" in {
        val result = {
          mockAuth()
          mockCreateOrAmendSavingsWithError(serviceUnavailableModel)
          controller.putInsurancePolicies(nino, taxYear)(fakeRequestWithMtditid.withJsonBody(Json.toJson(model)))
        }
        status(result) mustBe SERVICE_UNAVAILABLE
      }
      "the service returns a BAD_REQUEST" in {
        val result = {
          mockAuth()
          mockCreateOrAmendSavingsWithError(badRequestModel)
          controller.putInsurancePolicies(nino, taxYear)(fakeRequestWithMtditid.withJsonBody(Json.toJson(model)))
        }
        status(result) mustBe BAD_REQUEST
      }
      "the service returns a INTERNAL_SERVER_ERROR" in {
        val result = {
          mockAuth()
          mockCreateOrAmendSavingsWithError(internalServerErrorModel)
          controller.putInsurancePolicies(nino, taxYear)(fakeRequestWithMtditid.withJsonBody(Json.toJson(model)))
        }
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
