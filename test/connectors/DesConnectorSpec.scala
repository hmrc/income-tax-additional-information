/*
 * Copyright 2022 HM Revenue & Customs
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

import config.AppConfig
import org.scalamock.scalatest.MockFactory
import support.UnitTest
import support.providers.AppConfigStubProvider
import uk.gov.hmrc.http.HeaderNames._
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, SessionId}

class DesConnectorSpec extends UnitTest
  with MockFactory
  with AppConfigStubProvider {

  private val underTest = new DesConnector {
    override protected val appConfig: AppConfig = appConfigStub
  }

  ".base" should {
    "return the app config value" in {
      underTest.baseUrl shouldBe appConfigStub.desBaseUrl
    }
  }

  ".desHeaderCarrier" should {
    "return correct HeaderCarrier when internal host" in {
      val internalHost = "http://localhost"

      val result = underTest.desHeaderCarrier(internalHost)(HeaderCarrier())

      result.authorization shouldBe Some(Authorization(s"Bearer ${appConfigStub.authorisationToken}"))
      result.extraHeaders shouldBe Seq("Environment" -> appConfigStub.environment)
    }

    "return correct HeaderCarrier when external host" in {
      val externalHost = "http://127.0.0.1"
      val hc = HeaderCarrier(sessionId = Some(SessionId("sessionIdHeaderValue")))

      val result = underTest.desHeaderCarrier(externalHost)(hc)

      result.extraHeaders.size shouldBe 4
      result.extraHeaders.contains(xSessionId -> "sessionIdHeaderValue") shouldBe true
      result.extraHeaders.contains(authorisation -> s"Bearer ${appConfigStub.authorisationToken}") shouldBe true
      result.extraHeaders.contains("Environment" -> appConfigStub.environment) shouldBe true
      result.extraHeaders.exists(x => x._1.equalsIgnoreCase(xRequestChain)) shouldBe true
    }
  }
}
