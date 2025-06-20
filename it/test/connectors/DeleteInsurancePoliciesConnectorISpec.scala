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
import models.{ErrorBodyModel, ErrorModel}
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.http.Status._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class DeleteInsurancePoliciesConnectorISpec extends PlaySpec with WiremockSpec{

  lazy val connector: DeleteInsurancePoliciesConnector = app.injector.instanceOf[DeleteInsurancePoliciesConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val nino = "nino"
  val taxYear = 2023

  def mkTaxYear(taxYear: Int): String = {
    s"${taxYear - 1}-${taxYear.toString takeRight 2}"
  }
  val ifUrl = s"/income-tax/insurance-policies/income/$nino/${mkTaxYear(taxYear)}"

  lazy val httpClient: HttpClientV2 = app.injector.instanceOf[HttpClientV2]

  def appConfig(ifHost: String): AppConfig = new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override lazy val ifBaseUrl: String = s"http://$ifHost:$wireMockPort"
  }

  "DeleteInsurancePoliciesConnector " should {

    "include internal headers" when {
      val headersSentToIF = Seq(
        new HttpHeader(HeaderNames.authorisation, "Bearer secret"),
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      val internalHost = "localhost"
      val externalHost = "127.0.0.1"

      "the host for IF is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new DeleteInsurancePoliciesConnector(httpClient, appConfig(internalHost))

        stubDeleteWithoutResponseBody(ifUrl, NO_CONTENT, headersSentToIF)

        val result = await(connector.deleteInsurancePoliciesData(nino, taxYear)(hc))

        result mustBe Right(true)
      }

      "the host for IF is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new DeleteInsurancePoliciesConnector(httpClient, appConfig(externalHost))

        stubDeleteWithoutResponseBody(ifUrl, NO_CONTENT, headersSentToIF)

        val result = await(connector.deleteInsurancePoliciesData(nino, taxYear)(hc))

        result mustBe Right(true)
      }
    }

    "handle error" when {

      val errorBodyModel = ErrorBodyModel("IF_CODE", "IF_REASON")

      Seq(INTERNAL_SERVER_ERROR, NOT_FOUND, BAD_REQUEST).foreach { status =>

        s"If returns $status" in {
          val ifError = ErrorModel(status, errorBodyModel)
          implicit val hc: HeaderCarrier = HeaderCarrier()

          stubDeleteWithResponseBody(ifUrl, status, ifError.toJson.toString())

          val result = await(connector.deleteInsurancePoliciesData(nino, taxYear)(hc))

          result mustBe Left(ifError)
        }
      }

      "IF returns an unexpected error code - 502 BadGateway" in {
        val ifError = ErrorModel(INTERNAL_SERVER_ERROR, errorBodyModel)
        implicit val hc: HeaderCarrier = HeaderCarrier()

        stubDeleteWithResponseBody(ifUrl, BAD_GATEWAY, ifError.toJson.toString())

        val result = await(connector.deleteInsurancePoliciesData(nino, taxYear)(hc))

        result mustBe Left(ifError)
      }


      "IF returns error code - 503 ServiceUnavailable" in {
        val ifError = ErrorModel(INTERNAL_SERVER_ERROR, errorBodyModel)
        implicit val hc: HeaderCarrier = HeaderCarrier()

        stubDeleteWithResponseBody(ifUrl, SERVICE_UNAVAILABLE, ifError.toJson.toString())

        val result = await(connector.deleteInsurancePoliciesData(nino, taxYear)(hc))

        result mustBe Left(ifError)
      }

    }

  }
}
