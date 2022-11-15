


package connectors

import config.AppConfig
import org.scalamock.scalatest.MockFactory
import support.providers.AppConfigStubProvider
import support.stubs.AppConfigStub
import testUtils.TestSuite
import uk.gov.hmrc.http.HeaderNames._
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, SessionId}

class IFConnectorSpec extends TestSuite
  with MockFactory
  with AppConfigStubProvider {

  private val underTest = new IFConnector {
    override protected val appConfig: AppConfig = new AppConfigStub().config()
  }

  ".baseUrl" should {
    "return the app config + '/if' value when environment is test" in {
      val underTest = new IFConnector {
        override protected val appConfig: AppConfig = new AppConfigStub().config("test")
      }

      underTest.baseUrl mustBe appConfigStub.ifBaseUrl + "/if"
    }

    "return the app config value when environment is not test" in {
      val underTest = new IFConnector {
        override protected val appConfig: AppConfig = new AppConfigStub().config("not-test")
      }

      underTest.baseUrl mustBe appConfigStub.ifBaseUrl
    }
  }

  ".ifHeaderCarrier" should {
    "return correct HeaderCarrier when internal host" in {
      val internalHost = "http://localhost"

      val result = underTest.ifHeaderCarrier(internalHost, "some-api-version")(HeaderCarrier())

      result.authorization mustBe Some(Authorization(s"Bearer ${appConfigStub.authorisationTokenFor("some-api-version")}"))
      result.extraHeaders mustBe Seq("Environment" -> appConfigStub.ifEnvironment)
    }

    "return correct HeaderCarrier when external host" in {
      val externalHost = "http://127.0.0.1"
      val hc = HeaderCarrier(sessionId = Some(SessionId("sessionIdHeaderValue")))

      val result = underTest.ifHeaderCarrier(externalHost, "some-api-version")(hc)

      result.extraHeaders.size mustBe 4
      result.extraHeaders.contains(xSessionId -> "sessionIdHeaderValue") mustBe true
      result.extraHeaders.contains(authorisation -> s"Bearer ${appConfigStub.authorisationTokenFor("some-api-version")}") mustBe true
      result.extraHeaders.contains("Environment" -> appConfigStub.ifEnvironment) mustBe true
      result.extraHeaders.exists(x => x._1.equalsIgnoreCase(xRequestChain)) mustBe true
    }
  }
}