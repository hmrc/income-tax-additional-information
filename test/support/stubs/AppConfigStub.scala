

package support.stubs

import config.AppConfig
import org.scalamock.scalatest.MockFactory

class AppConfigStub extends MockFactory {

  def config(environment: String = "test"): AppConfig = new AppConfig() {
    private val wireMockPort = 11111
    override val authBaseUrl: String = "/auth"

    override val auditingEnabled: Boolean = true
    override val graphiteHost: String = "/graphite"

    override lazy val ifAuthorisationToken: String = ""

    override lazy val ifBaseUrl: String = s"http://localhost:$wireMockPort"
    override lazy val ifEnvironment: String = environment

    override def authorisationTokenFor(apiVersion: String): String = ifAuthorisationToken + s".$apiVersion"
  }
}