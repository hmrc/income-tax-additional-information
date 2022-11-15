package connectors

import config.AppConfig
import connectors.parsers.PutInsurancePoliciesParser.{InsurancePoliciesHttpReads, PutInsurancePoliciesResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.TaxYearUtils.convertStringTaxYear

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PutInsurancePoliciesConnector @Inject()(http: HttpClient, val appConfig: AppConfig)(implicit ec: ExecutionContext) extends IFConnector {

  def putInsurancePolicies(nino: String, taxYear: Int)(implicit hc:HeaderCarrier): Future[PutInsurancePoliciesResponse] = {
    val taxYearParameter = convertStringTaxYear(taxYear)
    val insurancePoliciesUrl = appConfig.ifBaseUrl + s"/income-tax/insurance-policies/income/$nino/$taxYearParameter"
    http.GET[PutInsurancePoliciesResponse](insurancePoliciesUrl)(InsurancePoliciesHttpReads, ifHeaderCarrier(insurancePoliciesUrl, PutInsurancePolicies), ec)
  }
}

