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
import connectors.parsers.PutInsurancePoliciesParser.{InsurancePoliciesHttpReads, PutInsurancePoliciesResponse}
import models.CreateOrAmendInsurancePoliciesModel
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.TaxYearUtils.convertStringTaxYear

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PutInsurancePoliciesConnector @Inject()(http: HttpClient, val appConfig: AppConfig)(implicit ec: ExecutionContext) extends IFConnector {

  def putInsurancePolicies(nino: String,
                           taxYear: Int,
                           insurancePoliciesModel: CreateOrAmendInsurancePoliciesModel)(implicit hc: HeaderCarrier): Future[PutInsurancePoliciesResponse] = {
    val taxYearParameter = convertStringTaxYear(taxYear)
    val insurancePoliciesUrl = appConfig.ifBaseUrl + s"/income-tax/insurance-policies/income/$nino/$taxYearParameter"

    def ifCall(implicit hc: HeaderCarrier): Future[PutInsurancePoliciesResponse] = {
      http.PUT[CreateOrAmendInsurancePoliciesModel, PutInsurancePoliciesResponse](insurancePoliciesUrl, insurancePoliciesModel)(
        CreateOrAmendInsurancePoliciesModel.formats.writes, InsurancePoliciesHttpReads, ifHeaderCarrier(insurancePoliciesUrl, PutInsurancePolicies), ec)    }
    ifCall(ifHeaderCarrier(insurancePoliciesUrl, PutInsurancePolicies))
  }
}