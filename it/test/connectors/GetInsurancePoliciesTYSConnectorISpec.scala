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
import helpers.WiremockSpec
import models._
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.TaxYearUtils.{convertSpecificTaxYear, specificTaxYear}

class GetInsurancePoliciesTYSConnectorISpec extends PlaySpec with WiremockSpec {

  lazy val connector: GetInsurancePoliciesTYSConnector = app.injector.instanceOf[GetInsurancePoliciesTYSConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val nino = "nino"
  val taxYearParameter = convertSpecificTaxYear(specificTaxYear)
  val url = s"/income-tax/insurance-policies/income/$taxYearParameter/$nino"

  lazy val httpClient: HttpClientV2 = app.injector.instanceOf[HttpClientV2]

  def appConfig(ifHost: String): AppConfig = new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
  }

  val ifReturned: InsurancePoliciesModel = InsurancePoliciesModel(
    submittedOn = "2020-01-04T05:01:01Z",
    lifeInsurance = Some(Seq(LifeInsuranceModel(Some("RefNo13254687"), Some("Life"), 123.45, Some(true), Some(4), Some(3), Some(123.45)))),
    capitalRedemption = Some(Seq(CapitalRedemptionModel(Some("RefNo13254687"), Some("Capital"), 123.45, Some(true), Some(3), Some(2), Some(0)))),
    lifeAnnuity = Some(Seq(LifeAnnuityModel(Some("RefNo13254687"), Some("Life"), 0, Some(true), Some(2), Some(22), Some(123.45)))),
    voidedIsa = Some(Seq(VoidedIsaModel(Some("RefNo13254687"), Some("isa"), 123.45, Some(123.45), Some(5), Some(6)))),
    foreign = Some(Seq(ForeignModel(Some("RefNo13254687"), 123.45, Some(123.45), Some(3))))
  )

  val ifReturnedEmpty: JsObject = Json.obj()

  " GetInsurancePoliciesTYSConnector" should {

    "include internal headers" when {

      val headersSentToDes = Seq(
        new HttpHeader(HeaderNames.authorisation, "Bearer secret"),
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      val externalHost = "127.0.0.1"

      "the host for IF is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

        stubGetWithResponseBody(url, OK, Json.toJson(ifReturned).toString)

        val result = await(connector.getInsurancePolicies(nino, specificTaxYear)(hc))

        result mustBe Right(ifReturned)
      }

      "the host for IF is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

        stubGetWithResponseBody(url, OK, Json.toJson(ifReturned).toString, headersSentToDes)

        val connector = new GetInsurancePoliciesTYSConnector(httpClient, appConfig(externalHost))

        val result = await(connector.getInsurancePolicies(nino, specificTaxYear)(hc))

        result mustBe Right(ifReturned)
      }
    }

    "return a success result" when {

      "IF returns a 200" in {
        stubGetWithResponseBody(url, OK, Json.toJson(ifReturned).toString)
        val result = await(connector.getInsurancePolicies(nino, specificTaxYear))

        result mustBe Right(ifReturned)

      }
    }

    "return an error" when {

      "IF returns an empty 200" in {
        stubGetWithResponseBody(url, OK, ifReturnedEmpty.toString())
        val result = await(connector.getInsurancePolicies(nino, specificTaxYear))
        val expectedResult = ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel.parsingError)

        result mustBe Left(expectedResult)

      }
    }

    "return a NoContent response" in {

      val expectedResult = ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel.parsingError)
      stubGetWithResponseBody(url, NO_CONTENT, "{}")

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getInsurancePolicies(nino, specificTaxYear)(hc))

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
      val result = await(connector.getInsurancePolicies(nino, specificTaxYear)(hc))

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
      val result = await(connector.getInsurancePolicies(nino, specificTaxYear)(hc))

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
      val result = await(connector.getInsurancePolicies(nino, specificTaxYear)(hc))

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
      val result = await(connector.getInsurancePolicies(nino, specificTaxYear)(hc))

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
      val result = await(connector.getInsurancePolicies(nino, specificTaxYear)(hc))

      result mustBe Left(expectedResult)
    }
  }

}
