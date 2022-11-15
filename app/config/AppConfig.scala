

package config

import com.google.inject.ImplementedBy
import javax.inject.Inject
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig


@ImplementedBy(classOf[BackendAppConfig])
trait AppConfig {
  val authBaseUrl: String
  val auditingEnabled: Boolean
  val graphiteHost: String

  val ifAuthorisationToken: String
  val ifBaseUrl: String
  val ifEnvironment: String

  def authorisationTokenFor(apiVersion: String): String
}


class BackendAppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) extends AppConfig {

  val authBaseUrl: String = servicesConfig.baseUrl("auth")

  val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")
  val graphiteHost: String = config.get[String]("microservice.metrics.graphite.host")

  lazy val ifAuthorisationToken: String = "microservice.services.integration-framework.authorisation-token"
  lazy val ifBaseUrl: String = servicesConfig.baseUrl(serviceName = "integration-framework")
  lazy val ifEnvironment: String = servicesConfig.getString(key = "microservice.services.integration-framework.environment")

  def authorisationTokenFor(api: String): String = config.get[String](s"microservice.services.integration-framework.authorisation-token.$api")
}