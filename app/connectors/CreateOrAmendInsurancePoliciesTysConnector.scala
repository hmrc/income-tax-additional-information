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
import connectors.parsers.CreateOrAmendInsurancePoliciesParser.{CreateOrAmendInsurancePoliciesResponse, InsurancePoliciesHttpReads}
import models.CreateOrAmendInsurancePoliciesModel
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import utils.TaxYearUtils.convertSpecificTaxYear

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CreateOrAmendInsurancePoliciesTysConnector @Inject()(http: HttpClientV2, val appConfig: AppConfig)(implicit ec: ExecutionContext) extends IFConnector {

  def createOrAmendInsurancePolicies(nino: String,
                                     taxYear: Int,
                                     createOrAmendinsurancePoliciesModel: CreateOrAmendInsurancePoliciesModel)(implicit hc: HeaderCarrier): Future[CreateOrAmendInsurancePoliciesResponse] = {
    val taxYearParameter = convertSpecificTaxYear(taxYear)

    val insurancePoliciesUrl = appConfig.ifBaseUrl + s"/income-tax/insurance-policies/income/$taxYearParameter/$nino"
    http.put(url"$insurancePoliciesUrl")(ifHeaderCarrier(insurancePoliciesUrl, PutInsurancePoliciesTys))
      .withBody(Json.toJson(createOrAmendinsurancePoliciesModel.clearModel)(CreateOrAmendInsurancePoliciesModel.formats.writes(_)))
      .execute[CreateOrAmendInsurancePoliciesResponse](InsurancePoliciesHttpReads, ec)
  }

}