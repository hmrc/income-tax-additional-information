package connectors

import com.typesafe.config.ConfigFactory
import config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier.Config
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import utils.HeaderCarrierSyntax.HeaderCarrierOps

import java.net.URL

trait IFConnector {

  protected val appConfig: AppConfig
  protected[connectors] lazy val baseUrl: String = if (appConfig.ifEnvironment == "test") appConfig.ifBaseUrl + "/if" else appConfig.ifBaseUrl
  val PutInsurancePolicies = "1614"

  protected val headerCarrierConfig: Config = HeaderCarrier.Config.fromConfig(ConfigFactory.load())

  protected[connectors] def ifHeaderCarrier(url: String, apiVersion: String)(implicit hc: HeaderCarrier): HeaderCarrier = {
    val isInternalHost = headerCarrierConfig.internalHostPatterns.exists(_.pattern.matcher(new URL(url).getHost).matches())
    val hcWithAuth = hc.copy(authorization = Some(Authorization(s"Bearer ${appConfig.authorisationTokenFor(apiVersion)}")))

    if (isInternalHost) {
      hcWithAuth.withExtraHeaders(headers = "Environment" -> appConfig.ifEnvironment)
    } else {
      hcWithAuth.withExtraHeaders(("Environment" -> appConfig.ifEnvironment) +: hcWithAuth.toExplicitHeaders: _*)
    }
  }
}