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

package connectors

import com.github.tomakehurst.wiremock.http.HttpHeader
import config.{AppConfig, BackendAppConfig}
import connectors.GetOtherEmploymentsIncomeConnector
import models._
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import test.helpers.WiremockSpec
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.TaxYearUtils.convertSpecificTaxYear

class GetOtherEmploymentsIncomeConnectorISpec extends PlaySpec with WiremockSpec {

  lazy val connector: GetOtherEmploymentsIncomeConnector = app.injector.instanceOf[GetOtherEmploymentsIncomeConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val nino = "nino"
  val taxYear = 2024
  val taxYearParameter = convertSpecificTaxYear(taxYear)
  val url = s"/income-tax/income/other/employments/$taxYearParameter/$nino"

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  def appConfig(ifHost: String): AppConfig = new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
  }

  val ifReturned: OtherEmploymentsIncomeModel = OtherEmploymentsIncomeModel(
    submittedOn = "2019-08-24T14:15:22Z",
    shareOption = Some(Seq(ShareOptionModel("string", Some("123/abc 001<Q>"), "EMI", "2019-08-24", "2019-08-24", Some(true),
      99999999999.99, 0, Some("string"), 99999999999.99, 99999999999.99, 99999999999.99, 99999999999.99,
      99999999999.99, 99999999999.99))),
    sharesAwardedOrReceived = Some(Seq(SharesAwardedOrReceivedModel("string", Some("123/abc 001<Q>"), "SIP", "2019-08-24", 0, "string",
      "2019-08-24", true, true, 99999999999.99, 99999999999.99, 99999999999.99,
      99999999999.99, 99999999999.99))),
    disability = Some(DisabilityModel(Some("string"), 99999999999.99)),
    foreignService = Some(ForeignServiceModel(Some("string"), 99999999999.99)),
    lumpSums = Some(Seq(LumpSumsModel("string", "123/abc 001<Q>",
      Some(TaxableLumpSumsAndCertainIncomeModel(99999999999.99, Some(99999999999.99), Some(true))),
      Some(BenefitFromEmployerFinancedRetirementSchemeModel(99999999999.99, Some(99999999999.99), Some(99999999999.99), Some(true))),
      Some(RedundancyCompensationPaymentsOverExemptionModel(99999999999.99, Some(99999999999.99), Some(true))),
      Some(RedundancyCompensationPaymentsUnderExemptionModel(99999999999.99)))))
  )

  " GetOtherEmploymentsIncomeConnector" should {

    "include internal headers" when {

      val headersSentToDes = Seq(
        new HttpHeader(HeaderNames.authorisation, "Bearer secret"),
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      val externalHost = "127.0.0.1"

      "the host for IF is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

        stubGetWithResponseBody(url, OK, Json.toJson(ifReturned).toString)

        val result = await(connector.getOtherEmploymentsIncome(nino, taxYear)(hc))

        result mustBe Right(ifReturned)
      }

      "the host for IF is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

        stubGetWithResponseBody(url, OK, Json.toJson(ifReturned).toString, headersSentToDes)

        val connector = new GetOtherEmploymentsIncomeConnector(httpClient, appConfig(externalHost))

        val result = await(connector.getOtherEmploymentsIncome(nino, taxYear)(hc))

        result mustBe Right(ifReturned)
      }
    }

    "return a success result" when {

      "IF returns a 200" in {
        stubGetWithResponseBody(url, OK, Json.toJson(ifReturned).toString)
        val result = await(connector.getOtherEmploymentsIncome(nino, taxYear))

        result mustBe Right(ifReturned)

      }
    }

    "return a NoContent response" in {

      val expectedResult = ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel.parsingError)
      stubGetWithResponseBody(url, NO_CONTENT, "{}")

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getOtherEmploymentsIncome(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a BadRequest response" in {

      val responseBody = Json.obj(
        "code" -> "INVALID_NINO",
        "reason" -> "NINO is invalid"
      )
      val expectedResult = ErrorModel(BAD_REQUEST, ErrorBodyModel("INVALID_NINO", "NINO is invalid"))
      stubGetWithResponseBody(url, BAD_REQUEST, responseBody.toString())

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getOtherEmploymentsIncome(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a UnprocessableEntity response" in {

      val responseBody = Json.obj(
        "code" -> "TAX_YEAR_NOT_SUPPORTED",
        "reason" -> "Tax year is not supported"
      )
      val expectedResult = ErrorModel(UNPROCESSABLE_ENTITY, ErrorBodyModel("TAX_YEAR_NOT_SUPPORTED", "Tax year is not supported"))
      stubGetWithResponseBody(url, UNPROCESSABLE_ENTITY, responseBody.toString())

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getOtherEmploymentsIncome(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a NotFound response" in {

      val responseBody = Json.obj(
        "code" -> "NOT_FOUND_INCOME_SOURCE",
        "reason" -> "Can't find the income source"
      )
      val expectedResult = ErrorModel(NOT_FOUND, ErrorBodyModel("NOT_FOUND_INCOME_SOURCE", "Can't find the income source"))
      stubGetWithResponseBody(url, NOT_FOUND, responseBody.toString())

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getOtherEmploymentsIncome(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return an InternalServerError response" in {

      val responseBody = Json.obj(
        "code" -> "SERVER_ERROR",
        "reason" -> "Internal Server Error"
      )
      val expectedResult = ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel("SERVER_ERROR", "Internal Server Error"))
      stubGetWithResponseBody(url, INTERNAL_SERVER_ERROR, responseBody.toString())

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getOtherEmploymentsIncome(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a ServiceUnavailable response" in {

      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "The service is currently unavailable"
      )
      val expectedResult = ErrorModel(SERVICE_UNAVAILABLE, ErrorBodyModel("SERVICE_UNAVAILABLE", "The service is currently unavailable"))
      stubGetWithResponseBody(url, SERVICE_UNAVAILABLE, responseBody.toString())

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getOtherEmploymentsIncome(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }
  }

}
