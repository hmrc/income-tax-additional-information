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

import models.gains.PolicyCyaModel
import models.mongo.{DataNotDeleted, DatabaseError, GainsUserDataModel, MongoError}
import models.{AllGainsSessionModel, User}
import org.scalamock.handlers.CallHandler3
import play.api.http.Status._
import services.GainsSessionService
import testUtils.TestSuite

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class DeleteGainsSessionDataControllerSpec extends TestSuite{

  val mockGainsSessionService: GainsSessionService = mock[GainsSessionService]
  val controller = new DeleteGainsSessionDataController(mockGainsSessionService, mockControllerComponents, authorisedAction)

  val nino = "nino"
  val taxYear = 2025
  val mtditid = "someMtditid"
  val sessionId = "sessionId-eb3158c2-0aff-4ce8-8d1b-f2208ace52fe"

  val completePolicyCyaModel: PolicyCyaModel =
    PolicyCyaModel(
      sessionId, Some("Life Insurance"), Some("123"), Some(0), Some(""), Some(true),
      Some(0), Some(0), Some(true), Some(123.11), Some(true), Some(123.11)
    )

  val validGainsSessionModel: AllGainsSessionModel = AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true))
  val completeGainsUserDataModel: GainsUserDataModel = GainsUserDataModel(sessionId, mtditid, nino, taxYear, Some(validGainsSessionModel), Instant.now)

  ".deleteSession" should {

    "return a 204 NO CONTENT if deletes insurance policies data successful" in {
      def serviceCallMock(): CallHandler3[Int, User[_], ExecutionContext, Future[Either[DatabaseError, Boolean]]] =
        (mockGainsSessionService.deleteSessionData( _: Int)(_: User[_], _: ExecutionContext))
          .expects(taxYear, *, *)
          .returning(Future.successful(Right(true)))

      val result = {
        mockAuth()
        serviceCallMock()
        controller.deleteSession(taxYear)(fakeRequest)
      }

      status(result) mustBe NO_CONTENT
    }

    "return a Left response" when {

      def mockServiceDeleteSessionData(mongoError: MongoError):
        CallHandler3[Int, User[_], ExecutionContext, Future[Either[DatabaseError, Boolean]]] =
          (mockGainsSessionService.deleteSessionData( _: Int)(_: User[_], _: ExecutionContext))
            .expects(taxYear, *, *)
            .returning(Future.successful(Left(mongoError)))

      "the service returns a INTERNAL_SERVER_ERROR" in {
        val result = {
          mockAuth()
          mockServiceDeleteSessionData(MongoError(DataNotDeleted.message))
          controller.deleteSession(taxYear)(fakeRequest)
        }
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
