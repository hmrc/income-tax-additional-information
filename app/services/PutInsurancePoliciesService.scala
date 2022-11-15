

package services

import connectors.PutInsurancePoliciesConnector

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import connectors.parsers.PutInsurancePoliciesParser.PutInsurancePoliciesResponse

import scala.concurrent.Future

@Singleton
class PutInsurancePoliciesService @Inject()(putInsurancePoliciesConnector: PutInsurancePoliciesConnector) {

  def putInsurancePolicies(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[PutInsurancePoliciesResponse] = {
    putInsurancePoliciesConnector.putInsurancePolicies(nino, taxYear)
  }

}