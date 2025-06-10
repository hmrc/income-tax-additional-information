/*
 * Copyright 2025 HM Revenue & Customs
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
import connectors.parsers.CreateOrUpdateOtherReliefsParser.{CreateOrUpdateOtherReliefsResponse, OtherReliefsHttpReads}
import models.otherReliefs.CreateOrUpdateOtherReliefsModel
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.TaxYearUtils.convertStringTaxYear

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CreateOrUpdateOtherReliefsConnector @Inject()(http: HttpClient, val appConfig: AppConfig)(implicit ec: ExecutionContext) extends IFConnector {

  // For connectivity to IF API#1632 - Create/Update Other Reliefs Submission
  def createOrUpdateOtherReliefs(nino: String,
                                 taxYear: Int,
                                 createOrUpdateOtherReliefsModel: CreateOrUpdateOtherReliefsModel)(implicit hc: HeaderCarrier): Future[CreateOrUpdateOtherReliefsResponse] = {
    val taxYearParameter = convertStringTaxYear(taxYear)
    val otherReliefsUrl = appConfig.ifBaseUrl + s"/income-tax/reliefs/other/$nino/$taxYearParameter"
      http.PUT[CreateOrUpdateOtherReliefsModel, CreateOrUpdateOtherReliefsResponse](otherReliefsUrl,
        createOrUpdateOtherReliefsModel.clearModel)(
        CreateOrUpdateOtherReliefsModel.formats.writes(_), OtherReliefsHttpReads, ifHeaderCarrier(otherReliefsUrl, PutOtherReliefs), ec)    }

}
