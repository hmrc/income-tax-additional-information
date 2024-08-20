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

import models._
import models.gains.PolicyCyaModel
import models.mongo.{DataNotFound, DatabaseError, MongoError}
import org.scalamock.handlers.CallHandler4
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import services.GainsSessionService
import testUtils.TestSuite

import scala.concurrent.{ExecutionContext, Future}

class UpdateGainsSessionDataControllerSpec extends TestSuite {

  val mockGainsSessionService: GainsSessionService = mock[GainsSessionService]
  val controller = new UpdateGainsSessionDataController(mockGainsSessionService, mockControllerComponents, authorisedAction)

  val nino = "nino"
  val taxYear = 2025
  val mtditid = "1234567890"
  val sessionId = "sessionId-eb3158c2-0aff-4ce8-8d1b-f2208ace52fe"

  val completePolicyCyaModel: PolicyCyaModel = PolicyCyaModel(
    sessionId, Some("Life Insurance"), Some("123"), Some(0), Some(""), Some(true), Some(0), Some(0), Some(true), Some(123.11), Some(true), Some(123.11)
  )

  val model: AllGainsSessionModel = AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true))

  val mongoError: MongoError = MongoError(DataNotFound.message)

  override val fakeRequestWithMtditid: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("PUT", "/").withHeaders("MTDITID" -> mtditid)

  ".createSession" should {

    "Return a 204 NO_CONTENT response with valid Session Data" in {
      def serviceCallMock(): CallHandler4[AllGainsSessionModel, Int, User[_], ExecutionContext, Future[Either[DatabaseError, Unit]]] =
        (mockGainsSessionService.updateSessionData(_: AllGainsSessionModel, _: Int)(_: User[_], _: ExecutionContext))
          .expects(model, taxYear, *, *)
          .returning(Future.successful(Right(())))

      val result = {
        mockAuth()
        serviceCallMock()
        controller.updateSession(taxYear)(fakeRequestWithMtditid.withJsonBody(Json.toJson(model)))
      }
      status(result) mustBe NO_CONTENT
    }

    "return a Left response" when {

      "the service returns a BAD_REQUEST" in {
        val result = {
          mockAuth()
          controller.updateSession(taxYear)()(fakeRequestWithMtditid.withJsonBody(Json.toJson("InvalidAllGainsSessionModel")))
        }

        status(result) mustBe BAD_REQUEST
      }

      "the service returns a INTERNAL_SERVER_ERROR" in {
        def mockCreateOrAmendCreateSessionWithError(mongoError: MongoError):
          CallHandler4[AllGainsSessionModel, Int, User[_], ExecutionContext, Future[Either[DatabaseError, Unit]]] = {

          (mockGainsSessionService.updateSessionData(_: AllGainsSessionModel, _: Int)(_: User[_], _: ExecutionContext))
            .expects(model, taxYear, *, *)
            .returning(Future.successful(Left(mongoError)))
        }

        val result = {
          mockAuth()
          mockCreateOrAmendCreateSessionWithError(mongoError)
          controller.updateSession(taxYear)(fakeRequestWithMtditid.withJsonBody(Json.toJson(model)))
        }
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
