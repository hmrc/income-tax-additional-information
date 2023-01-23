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

import connectors.parsers.CreateOrAmendInsurancePoliciesParser.CreateOrAmendInsurancePoliciesResponse
import models._
import org.scalamock.handlers.CallHandler4
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import services.CreateOrAmendInsurancePoliciesService
import testUtils.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class CreateOrAmendInsurancePoliciesControllerSpec extends TestSuite {

  val serviceMock: CreateOrAmendInsurancePoliciesService = mock[CreateOrAmendInsurancePoliciesService]
  val controller = new CreateOrAmendInsurancePoliciesController(serviceMock, mockControllerComponents, authorisedAction)

  val notFoundModel: ErrorModel = ErrorModel(NOT_FOUND, ErrorBodyModel("NotFound", "Unable to find source"))
  val serviceUnavailableModel: ErrorModel =
    ErrorModel(SERVICE_UNAVAILABLE, ErrorBodyModel("SERVICE_UNAVAILABLE", "The service is currently unavailable"))
  val badRequestModel: ErrorModel = ErrorModel(BAD_REQUEST, ErrorBodyModel("BAD_REQUEST", "The supplied NINO is invalid"))
  val internalServerErrorModel: ErrorModel =
    ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel("INTERNAL_SERVER_ERROR", "There has been an unexpected error"))


  val nino = "nino"
  val taxYear = 2023
  val mtditid = "1234567890"

  val model: CreateOrAmendInsurancePoliciesModel = CreateOrAmendInsurancePoliciesModel(
    lifeInsurance = Seq(LifeInsuranceModel(Some("RefNo13254687"), Some("Life"), 123.45, Some(true), Some(4), Some(3), Some(123.45))),
    capitalRedemption = Some(Seq(CapitalRedemptionModel(Some("RefNo13254687"), Some("Capital"), 123.45, Some(true), Some(3), Some(2), Some(0)))),
    lifeAnnuity = Some(Seq(LifeAnnuityModel(Some("RefNo13254687"), Some("Life"), 0, Some(true), Some(2), Some(22), Some(123.45)))),
    voidedIsa = Some(Seq(VoidedIsaModel(Some("RefNo13254687"), Some("isa"), 123.45, Some(123.45), Some(5), Some(6)))),
    foreign = Some(Seq(ForeignModel(Some("RefNo13254687"), 123.45, Some(123.45), Some(3))))
  )

  override val fakeRequestWithMtditid: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("PUT", "/").withHeaders("MTDITID" -> mtditid)

  ".putInsurancePolicies" should {

    "Return a 204 NO Content response with valid putInsurancePolicies" in {

      val serviceResult = Right(true)
      val finalResult = Json.toJson(model).toString()

      def serviceCallMock(): CallHandler4[String, Int, CreateOrAmendInsurancePoliciesModel, HeaderCarrier, Future[CreateOrAmendInsurancePoliciesResponse]] =
        (serviceMock.createOrAmendInsurancePolicies(_: String, _: Int, _: CreateOrAmendInsurancePoliciesModel)(_: HeaderCarrier))
          .expects(nino, taxYear, model, *)
          .returning(Future.successful(serviceResult))

      val result = {
        mockAuth()
        serviceCallMock()
        controller.createOrAmendInsurancePolicies(nino, taxYear)(fakeRequestWithMtditid.withJsonBody(Json.toJson(model)))
      }
      status(result) mustBe NO_CONTENT
    }

    "return a Left response" when {

      def mockCreateOrAmendInsurancePoliciesWithError(errorModel: ErrorModel): CallHandler4[String, Int, CreateOrAmendInsurancePoliciesModel, HeaderCarrier, Future[CreateOrAmendInsurancePoliciesResponse]] = {
        (serviceMock.createOrAmendInsurancePolicies(_: String, _: Int, _: CreateOrAmendInsurancePoliciesModel)(_: HeaderCarrier))
          .expects(nino, taxYear, *, *)
          .returning(Future.successful(Left(errorModel)))
      }

      "the service returns a NO_CONTENT" in {
        val result = {
          mockAuth()
          mockCreateOrAmendInsurancePoliciesWithError(notFoundModel)
          controller.createOrAmendInsurancePolicies(nino, taxYear)(fakeRequestWithMtditid.withJsonBody(Json.toJson(model)))
        }
        status(result) mustBe NOT_FOUND
      }

      "the service returns a SERVICE_UNAVAILABLE" in {
        val result = {
          mockAuth()
          mockCreateOrAmendInsurancePoliciesWithError(serviceUnavailableModel)
          controller.createOrAmendInsurancePolicies(nino, taxYear)(fakeRequestWithMtditid.withJsonBody(Json.toJson(model)))
        }
        status(result) mustBe SERVICE_UNAVAILABLE
      }
      "the service returns a BAD_REQUEST" in {
        val result = {
          mockAuth()
          mockCreateOrAmendInsurancePoliciesWithError(badRequestModel)
          controller.createOrAmendInsurancePolicies(nino, taxYear)(fakeRequestWithMtditid.withJsonBody(Json.toJson(model)))
        }
        status(result) mustBe BAD_REQUEST
      }
      "the service returns a INTERNAL_SERVER_ERROR" in {
        val result = {
          mockAuth()
          mockCreateOrAmendInsurancePoliciesWithError(internalServerErrorModel)
          controller.createOrAmendInsurancePolicies(nino, taxYear)(fakeRequestWithMtditid.withJsonBody(Json.toJson(model)))
        }
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
