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

import config.AppConfig
import connectors.parsers.GetInsurancePoliciesParser.{GetInsurancePoliciesResponse, InsurancePoliciesHttpReads}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import utils.TaxYearUtils.convertStringTaxYear

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GetInsurancePoliciesConnector @Inject()(http: HttpClientV2, val appConfig: AppConfig)(implicit ec: ExecutionContext) extends IFConnector {

  def getInsurancePolicies(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetInsurancePoliciesResponse] = {
    val taxYearParameter = convertStringTaxYear(taxYear)
    val insurancePoliciesUrl = appConfig.ifBaseUrl + s"/income-tax/insurance-policies/income/$nino/$taxYearParameter"
    http.get(url"$insurancePoliciesUrl")(ifHeaderCarrier(insurancePoliciesUrl, GetInsurancePolicies))
      .execute[GetInsurancePoliciesResponse](InsurancePoliciesHttpReads, ec)
  }
}
