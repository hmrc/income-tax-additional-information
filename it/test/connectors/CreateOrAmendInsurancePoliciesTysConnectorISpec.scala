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

package test.connectors

import com.github.tomakehurst.wiremock.http.HttpHeader
import config.{AppConfig, BackendAppConfig}
import test.helpers.WiremockSpec
import models._
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.TaxYearUtils.{convertSpecificTaxYear, specificTaxYear}

class CreateOrAmendInsurancePoliciesTysConnectorISpec extends PlaySpec with WiremockSpec {

  val model: CreateOrAmendInsurancePoliciesModel = CreateOrAmendInsurancePoliciesModel(
    lifeInsurance = Some(Seq(LifeInsuranceModel(Some("RefNo13254687"), Some("Life"), 123.45, Some(true), Some(4), Some(3), Some(123.45)))),
    capitalRedemption = Some(Seq(CapitalRedemptionModel(Some("RefNo13254687"), Some("Capital"), 123.45, Some(true), Some(3), Some(2), Some(0)))),
    lifeAnnuity = Some(Seq(LifeAnnuityModel(Some("RefNo13254687"), Some("Life"), 0, Some(true), Some(2), Some(22), Some(123.45)))),
    voidedIsa = Some(Seq(VoidedIsaModel(Some("RefNo13254687"), Some("isa"), 123.45, Some(123.45), Some(5), Some(6)))),
    foreign = Some(Seq(ForeignModel(Some("RefNo13254687"), 123.45, Some(123.45), Some(3))))
  )

  val modelEmpty: JsObject = Json.obj()


  implicit val hc: HeaderCarrier = HeaderCarrier()
  lazy val connector: CreateOrAmendInsurancePoliciesTysConnector = app.injector.instanceOf[CreateOrAmendInsurancePoliciesTysConnector]
  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  val nino = "nino"
  val url = s"/income-tax/insurance-policies/income/${convertSpecificTaxYear(specificTaxYear)}/$nino"

  def appConfig(ifHost: String): AppConfig = new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
  }

  " PutInsurancePoliciesTysConnector" should {

    "include internal headers" when {
      val requestBody = Json.toJson(model).toString()

      val headersSentToIf = Seq(
        new HttpHeader(HeaderNames.authorisation, "Bearer secret"),
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      val externalHost = "127.0.0.1"

      "the host for IF is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val expectedResult = true

        stubPutWithoutResponseBody(url, CREATED, requestBody, headersSentToIf)

        val result = await(connector.createOrAmendInsurancePolicies(nino, specificTaxYear, model)(hc))

        result mustBe Right(expectedResult)
      }

      "the host for IF is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val expectedResult = true

        val connector = new CreateOrAmendInsurancePoliciesTysConnector(httpClient, appConfig(externalHost))

        stubPutWithoutResponseBody(url, CREATED, requestBody, headersSentToIf)

        val result = await(connector.createOrAmendInsurancePolicies(nino, specificTaxYear, model)(hc))

        result mustBe Right(expectedResult)
      }
    }

    "return a success result" when {
      "IF Returns a 200" in {
        val expectedResult = true

        stubPutWithoutResponseBody(url, CREATED, Json.toJson(model).toString())

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.createOrAmendInsurancePolicies(nino, specificTaxYear, model)(hc))

        result mustBe Right(expectedResult)
      }
    }
    "return a InternalServerError parsing error when incorrectly parsed" in {

      val invalidJson = Json.obj(
        "notErrormodel" -> "test"
      )

      val expectedResult = ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel.parsingError)

      stubPutWithResponseBody(url, INTERNAL_SERVER_ERROR, Json.toJson(model).toString(), invalidJson.toString)
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.createOrAmendInsurancePolicies(nino, specificTaxYear, model)(hc))

      result mustBe Left(expectedResult)
    }
    "return a failed result" when {
      "IF Returns a BAD_REQUEST" in {
        val expectedResult = ErrorModel(BAD_REQUEST, ErrorBodyModel("INVALID_IDTYPE", "ID is invalid"))

        val responseBody = Json.obj(
          "code" -> "INVALID_IDTYPE",
          "reason" -> "ID is invalid"
        )
        stubPutWithResponseBody(url, BAD_REQUEST, Json.toJson(model).toString(), responseBody.toString)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.createOrAmendInsurancePolicies(nino, specificTaxYear, model)(hc))

        result mustBe Left(expectedResult)
      }

      "IF Returns multiple errors" in {
        val expectedResult = ErrorModel(BAD_REQUEST, ErrorsBodyModel(Seq(
          ErrorBodyModel("INVALID_IDTYPE", "ID is invalid"),
          ErrorBodyModel("INVALID_IDTYPE_2", "ID 2 is invalid"))))

        val responseBody = Json.obj(
          "failures" -> Json.arr(
            Json.obj("code" -> "INVALID_IDTYPE",
              "reason" -> "ID is invalid"),
            Json.obj("code" -> "INVALID_IDTYPE_2",
              "reason" -> "ID 2 is invalid")
          )
        )
        stubPutWithResponseBody(url, BAD_REQUEST, Json.toJson(model).toString(), responseBody.toString)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.createOrAmendInsurancePolicies(nino, specificTaxYear, model)(hc))

        result mustBe Left(expectedResult)
      }

      "IF Returns a SERVICE_UNAVAILABLE" in {
        val expectedResult = ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel("SERVICE_UNAVAILABLE", "The service is currently unavailable"))

        val responseBody = Json.obj(
          "code" -> "SERVICE_UNAVAILABLE",
          "reason" -> "The service is currently unavailable"
        )
        stubPutWithResponseBody(url, SERVICE_UNAVAILABLE, Json.toJson(model).toString(), responseBody.toString)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.createOrAmendInsurancePolicies(nino, specificTaxYear, model)(hc))

        result mustBe Left(expectedResult)
      }

      "IF Returns a NOT_FOUND" in {
        val expectedResult = ErrorModel(NOT_FOUND, ErrorBodyModel("NOT_FOUND", "Submission Period not found"))

        val responseBody = Json.obj(
          "code" -> "NOT_FOUND",
          "reason" -> "Submission Period not found"
        )
        stubPutWithResponseBody(url, NOT_FOUND, Json.toJson(model).toString(), responseBody.toString)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.createOrAmendInsurancePolicies(nino, specificTaxYear, model)(hc))

        result mustBe Left(expectedResult)
      }

      "IF Returns a UNPROCESSABLE_ENTITY" in {
        val expectedResult = ErrorModel(BAD_REQUEST, ErrorBodyModel("UNPROCESSABLE_ENTITY", "The remote endpoint has " +
          "indicated that for given income source type, message payload is incorrect."))

        val responseBody = Json.obj(
          "code" -> "UNPROCESSABLE_ENTITY",
          "reason" -> "The remote endpoint has indicated that for given income source type, message payload is incorrect."
        )
        stubPutWithResponseBody(url, UNPROCESSABLE_ENTITY, Json.toJson(model).toString(), responseBody.toString)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.createOrAmendInsurancePolicies(nino, specificTaxYear, model)(hc))

        result mustBe Left(expectedResult)
      }

      "IF Returns a INTERNAL_SERVER_ERROR" in {
        val expectedResult = ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel("SERVER_ERROR", "Internal Server Error"))

        val responseBody = Json.obj(
          "code" -> "SERVER_ERROR",
          "reason" -> "Internal Server Error"
        )
        stubPutWithResponseBody(url, INTERNAL_SERVER_ERROR, Json.toJson(model).toString(), responseBody.toString())

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.createOrAmendInsurancePolicies(nino, specificTaxYear, model)(hc))

        result mustBe Left(expectedResult)
      }

      "IF Returns a unexpected response" in {
        val expectedResult = ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel.parsingError)

        stubPutWithoutResponseBody(url, GONE, Json.toJson(model).toString())

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.createOrAmendInsurancePolicies(nino, specificTaxYear, model)(hc))

        result mustBe Left(expectedResult)
      }
    }
  }

}