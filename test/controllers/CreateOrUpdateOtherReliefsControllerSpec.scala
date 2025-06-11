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

package controllers

import connectors.parsers.CreateOrUpdateOtherReliefsParser.CreateOrUpdateOtherReliefsResponse
import models._
import models.otherReliefs._
import org.scalamock.handlers.CallHandler4
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import services.CreateOrUpdateOtherReliefsService
import testUtils.TestSuite
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

class CreateOrUpdateOtherReliefsControllerSpec extends TestSuite {

  val serviceMock: CreateOrUpdateOtherReliefsService = mock[CreateOrUpdateOtherReliefsService]
  val controller = new CreateOrUpdateOtherReliefsController(serviceMock, mockControllerComponents, authorisedAction)

  val serviceUnavailableModel: ErrorModel =
    ErrorModel(SERVICE_UNAVAILABLE, ErrorBodyModel("SERVICE_UNAVAILABLE", "The service is currently unavailable"))
  val badRequestModel: ErrorModel = ErrorModel(BAD_REQUEST, ErrorBodyModel("BAD_REQUEST", "The supplied NINO is invalid"))
  val internalServerErrorModel: ErrorModel =
    ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel("INTERNAL_SERVER_ERROR", "There has been an unexpected error"))
  val unprocessableEntityModel: ErrorModel =
    ErrorModel(UNPROCESSABLE_ENTITY, ErrorBodyModel("UNPROCESSABLE_ENTITY", "The remote endpoint has indicated that for given income source type, message payload is incorrect."))


  val nino = "nino"
  val taxYear = 2023
  val mtditid = "1234567890"

  val model: CreateOrUpdateOtherReliefsModel = CreateOrUpdateOtherReliefsModel(
    nonDeductableLoanInterest = Some(NonDeductableLoanInterestModel(Some("RefNo13254687"), 123.45)),
    payrollGiving = Some(PayrollGivingModel(Some("RefNo13254687"), 123.46)),
    qualifyingDistributionRedemptionOfSharesAndSecurities = Some(QualifyingDistributionModel(Some("RefNo13254687"), 123.47)),
    maintenancePayments = Some(Seq(MaintenancePaymentsModel(Some("RefNo13254687"), Some("Jane Doe"), Some("01-01-2000"), 123.48))),
    postCessationTradeReliefAndCertainOtherLosses = Some(Seq(PostCessationTradeReliefModel(Some("RefNo13254687"), Some("Monsters Inc"), Some("01-01-2025"), Some("Power Distribution"), Some("Cash"), 123.49))),
    annualPaymentsMade = Some(AnnualPaymentsMadeModel(Some("RefNo13254687"), 123.50)),
    qualifyingLoanInterestPayments = Some(Seq(QualifyingLoanInterestPaymentsModel(Some("RefNo13254687"), Some("Bank"), 123.51))),
  )

  override val fakeRequestWithMtditid: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("PUT", "/")
    .withHeaders("MTDITID" -> mtditid, SessionKeys.sessionId -> "some-session-id")

  ".putOtherReliefs" should {

    "Return a 204 NO Content response with valid putOtherReliefs" in {
      val serviceResult = Right(true)

      def serviceCallMock(): CallHandler4[String, Int, CreateOrUpdateOtherReliefsModel, HeaderCarrier, Future[CreateOrUpdateOtherReliefsResponse]] =
        (serviceMock.createOrUpdateOtherReliefs(_: String, _: Int, _: CreateOrUpdateOtherReliefsModel)(_: HeaderCarrier))
          .expects(nino, taxYear, model, *)
          .returning(Future.successful(serviceResult))

      val result: Future[Result] = {
        mockAuth()
        serviceCallMock()
        controller.createOrUpdateOtherReliefs(nino, taxYear)(fakeRequestWithMtditid.withJsonBody(Json.toJson(model)))
      }
      status(result) mustBe NO_CONTENT
    }

    "Return a 400 BAD_REQUEST response with invalid putOtherReliefs" in {
      val result = {
        mockAuth()
        controller.createOrUpdateOtherReliefs(nino, taxYear)()(fakeRequestWithMtditid.withJsonBody(Json.toJson("InvalidOtherReliefsModel")))
      }

      status(result) mustBe BAD_REQUEST
    }

    "return a Left response" when {

      def mockCreateOrUpdateOtherReliefsWithError(errorModel: ErrorModel):
        CallHandler4[String, Int, CreateOrUpdateOtherReliefsModel, HeaderCarrier, Future[CreateOrUpdateOtherReliefsResponse]] = {
          (serviceMock.createOrUpdateOtherReliefs(_: String, _: Int, _: CreateOrUpdateOtherReliefsModel)(_: HeaderCarrier))
            .expects(nino, taxYear, *, *)
            .returning(Future.successful(Left(errorModel)))
      }

      "the service returns a BAD_REQUEST" in {
        val result = {
          mockAuth()
          mockCreateOrUpdateOtherReliefsWithError(badRequestModel)
          controller.createOrUpdateOtherReliefs(nino, taxYear)(fakeRequestWithMtditid.withJsonBody(Json.toJson(model)))
        }
        status(result) mustBe BAD_REQUEST
      }

      "the service returns a UNPROCESSABLE_ENTITY" in {
        val result = {
          mockAuth()
          mockCreateOrUpdateOtherReliefsWithError(unprocessableEntityModel)
          controller.createOrUpdateOtherReliefs(nino, taxYear)(fakeRequestWithMtditid.withJsonBody(Json.toJson(model)))
        }
        status(result) mustBe UNPROCESSABLE_ENTITY
      }

      "the service returns a INTERNAL_SERVER_ERROR" in {
        val result = {
          mockAuth()
          mockCreateOrUpdateOtherReliefsWithError(internalServerErrorModel)
          controller.createOrUpdateOtherReliefs(nino, taxYear)(fakeRequestWithMtditid.withJsonBody(Json.toJson(model)))
        }
        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "the service returns a SERVICE_UNAVAILABLE" in {
        val result = {
          mockAuth()
          mockCreateOrUpdateOtherReliefsWithError(serviceUnavailableModel)
          controller.createOrUpdateOtherReliefs(nino, taxYear)(fakeRequestWithMtditid.withJsonBody(Json.toJson(model)))
        }
        status(result) mustBe SERVICE_UNAVAILABLE
      }
    }
  }

}
