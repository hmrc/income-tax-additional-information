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
import models.mongo.{DataNotFound, DatabaseError, GainsUserDataModel, MongoError}
import org.scalamock.handlers.CallHandler3
import play.api.http.Status._
import play.api.libs.json.Json
import services.GainsSessionService
import testUtils.TestSuite

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class GetGainsSessionDataControllerSpec extends TestSuite {

  val mockGainsSessionService: GainsSessionService = mock[GainsSessionService]
  val controller = new GetGainsSessionDataController(mockGainsSessionService, mockControllerComponents, authorisedAction)

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

  ".getSessionData" should {

    "return a 200 OK response with Gains User Data Model" in {
      def serviceCallMock(): CallHandler3[Int, User[_], ExecutionContext, Future[Either[DatabaseError, Option[GainsUserDataModel]]]] =
        (mockGainsSessionService.getSessionData( _: Int)(_: User[_], _: ExecutionContext))
          .expects(taxYear, *, *)
          .returning(Future.successful(Right(Some(completeGainsUserDataModel))))

      val result = {
        mockAuth()
        serviceCallMock()
        controller.getSession(taxYear)(fakeRequest)
      }

      status(result) mustBe OK
      bodyOf(result) mustBe Json.toJson(completeGainsUserDataModel).toString()
    }

    "return a 204 NO_CONTENT response with no Gains User Data Model" in {
      def serviceCallMock(): CallHandler3[Int, User[_], ExecutionContext, Future[Either[DatabaseError, Option[GainsUserDataModel]]]] =
        (mockGainsSessionService.getSessionData( _: Int)(_: User[_], _: ExecutionContext))
          .expects(taxYear, *, *)
          .returning(Future.successful(Right(None)))

      val result = {
        mockAuth()
        serviceCallMock()
        controller.getSession(taxYear)(fakeRequest)
      }

      status(result) mustBe NO_CONTENT
      bodyOf(result) mustBe ""
    }

    "return a Left response" when {

      def mockServiceGetSessionData(mongoError: MongoError):
        CallHandler3[Int, User[_], ExecutionContext, Future[Either[DatabaseError, Option[GainsUserDataModel]]]] =
          (mockGainsSessionService.getSessionData( _: Int)(_: User[_], _: ExecutionContext))
            .expects(taxYear, *, *)
            .returning(Future.successful(Left(mongoError)))

      "the service returns a NO_CONTENT" in {
        val result = {
          mockAuth()
          mockServiceGetSessionData(MongoError(DataNotFound.message))
          controller.getSession(taxYear)(fakeRequest)
        }
        status(result) mustBe NOT_FOUND
      }
    }
  }

}
