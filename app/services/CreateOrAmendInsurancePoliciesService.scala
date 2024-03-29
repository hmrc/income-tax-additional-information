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

package services

import connectors.{CreateOrAmendInsurancePoliciesConnector, CreateOrAmendInsurancePoliciesTysConnector}

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import connectors.parsers.CreateOrAmendInsurancePoliciesParser.CreateOrAmendInsurancePoliciesResponse
import models.CreateOrAmendInsurancePoliciesModel
import utils.TaxYearUtils.specificTaxYear

import scala.concurrent.Future

@Singleton
class CreateOrAmendInsurancePoliciesService @Inject()(createOrAmendInsurancePoliciesConnector: CreateOrAmendInsurancePoliciesConnector,
                                                      createOrAmendInsurancePoliciesTysConnector: CreateOrAmendInsurancePoliciesTysConnector) {

  def createOrAmendInsurancePolicies(nino: String, taxYear: Int, createOrAmendInsurancePoliciesModel: CreateOrAmendInsurancePoliciesModel)
                                    (implicit hc: HeaderCarrier): Future[CreateOrAmendInsurancePoliciesResponse] = {
    if (taxYear >= specificTaxYear) {
        createOrAmendInsurancePoliciesTysConnector.createOrAmendInsurancePolicies(nino, taxYear, createOrAmendInsurancePoliciesModel)
    } else {
      createOrAmendInsurancePoliciesConnector.createOrAmendInsurancePolicies(nino, taxYear, createOrAmendInsurancePoliciesModel)
    }
  }

}