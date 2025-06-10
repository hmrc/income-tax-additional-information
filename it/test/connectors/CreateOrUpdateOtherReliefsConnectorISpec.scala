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

package connectors

import com.github.tomakehurst.wiremock.http.HttpHeader
import config.{AppConfig, BackendAppConfig}
import helpers.WiremockSpec
import models._
import models.otherReliefs._
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.TaxYearUtils.convertStringTaxYear

class CreateOrUpdateOtherReliefsConnectorISpec extends PlaySpec with WiremockSpec {

  lazy val connector: CreateOrUpdateOtherReliefsConnector = app.injector.instanceOf[CreateOrUpdateOtherReliefsConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val nino = "nino"
  val taxYear = 2023
  val taxYearParameter = convertStringTaxYear(taxYear)

  val url = s"/income-tax/reliefs/other/$nino/$taxYearParameter"

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  def appConfig: AppConfig = new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig])

  val model: CreateOrUpdateOtherReliefsModel = CreateOrUpdateOtherReliefsModel(
    nonDeductableLoanInterest = Some(NonDeductableLoanInterestModel(Some("RefNo13254687"), 123.45)),
    payrollGiving = Some(PayrollGivingModel(Some("RefNo13254687"), 123.46)),
    qualifyingDistributionRedemptionOfSharesAndSecurities = Some(QualifyingDistributionModel(Some("RefNo13254687"), 123.47)),
    maintenancePayments = Some(Seq(MaintenancePaymentsModel(Some("RefNo13254687"), Some("Jane Doe"), Some("01-01-2000"), 123.48))),
    postCessationTradeReliefAndCertainOtherLosses = Some(Seq(PostCessationTradeReliefModel(Some("RefNo13254687"), Some("Monsters Inc"), Some("01-01-2025"), Some("Power Distribution"), Some("Cash"), 123.49))),
    annualPaymentsMade = Some(AnnualPaymentsMadeModel(Some("RefNo13254687"), 123.50)),
    qualifyingLoanInterestPayments = Some(Seq(QualifyingLoanInterestPaymentsModel(Some("RefNo13254687"), Some("Bank"), 123.51))),
  )

  val modelEmpty: JsObject = Json.obj()

  " PutOtherReliefsConnector" should {

    "include internal headers" when {
      val requestBody = Json.toJson(model).toString()

      val headersSentToIf = Seq(
        new HttpHeader(HeaderNames.authorisation, "Bearer secret"),
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      "the host for IF is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val expectedResult = true

        stubPutWithoutResponseBody(url, NO_CONTENT, requestBody, headersSentToIf)

        val result = await(connector.createOrUpdateOtherReliefs(nino, taxYear, model)(hc))

        result mustBe Right(expectedResult)
      }

      "the host for IF is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val expectedResult = true

        val connector = new CreateOrUpdateOtherReliefsConnector(httpClient, appConfig)

        stubPutWithoutResponseBody(url, NO_CONTENT, requestBody, headersSentToIf)

        val result = await(connector.createOrUpdateOtherReliefs(nino, taxYear, model)(hc))

        result mustBe Right(expectedResult)
      }
    }

    "return a success result" when {
      "IF Returns a 204" in {
        val expectedResult = true

        stubPutWithoutResponseBody(url, NO_CONTENT, Json.toJson(model).toString())

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.createOrUpdateOtherReliefs(nino, taxYear, model)(hc))

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
      val result = await(connector.createOrUpdateOtherReliefs(nino, taxYear, model)(hc))

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
        val result = await(connector.createOrUpdateOtherReliefs(nino, taxYear, model)(hc))

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
        val result = await(connector.createOrUpdateOtherReliefs(nino, taxYear, model)(hc))

        result mustBe Left(expectedResult)
      }

      "IF Returns a SERVICE_UNAVAILABLE" in {
        val expectedResult = ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel("SERVICE_UNAVAILABLE", "The service is currently unavailable"))

        val responseBody = Json.obj(
          "code" -> "SERVICE_UNAVAILABLE",
          "reason" -> "The service is currently unavailable"
        )
        stubPutWithResponseBody(url, INTERNAL_SERVER_ERROR, Json.toJson(model).toString(), responseBody.toString)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.createOrUpdateOtherReliefs(nino, taxYear, model)(hc))

        result mustBe Left(expectedResult)
      }

      "IF Returns a UNPROCESSABLE_ENTITY" in {
        val expectedResult = ErrorModel(BAD_REQUEST, ErrorBodyModel("UNPROCESSABLE_ENTITY", "The remote endpoint has indicated that for given income source type, message payload is incorrect."))

        val responseBody = Json.obj(
          "code" -> "UNPROCESSABLE_ENTITY",
          "reason" -> "The remote endpoint has indicated that for given income source type, message payload is incorrect."
        )
        stubPutWithResponseBody(url, BAD_REQUEST, Json.toJson(model).toString(), responseBody.toString)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.createOrUpdateOtherReliefs(nino, taxYear, model)(hc))

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
        val result = await(connector.createOrUpdateOtherReliefs(nino, taxYear, model)(hc))

        result mustBe Left(expectedResult)
      }
    }
  }

}
