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

package connectors.parsers


import models.ErrorModel
import play.api.Logging
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.PagerDutyHelper._

object DeleteInsurancePoliciesParser extends APIParser with Logging {
  type DeleteInsurancePoliciesResponse = Either[ErrorModel, Boolean]

  implicit object DeleteInsurancePoliciesHttpReads extends HttpReads[DeleteInsurancePoliciesResponse] {
    override def read(method: String, url: String, response: HttpResponse): DeleteInsurancePoliciesResponse = response.status match {

      case NO_CONTENT => Right(true)
      case NOT_FOUND =>
        logger.info(logMessage(response))
        handleAPIError(response)
      case INTERNAL_SERVER_ERROR =>
        pagerDutyLog(INTERNAL_SERVER_ERROR_FROM_API, logMessage(response))
        handleAPIError(response, Some(INTERNAL_SERVER_ERROR))
      case SERVICE_UNAVAILABLE =>
        pagerDutyLog(SERVICE_UNAVAILABLE_FROM_API, logMessage(response))
        handleAPIError(response, Some(INTERNAL_SERVER_ERROR))
      case BAD_REQUEST | NOT_FOUND =>
        pagerDutyLog(FOURXX_RESPONSE_FROM_API, logMessage(response))
        handleAPIError(response)
      case UNPROCESSABLE_ENTITY =>
        pagerDutyLog(TAX_YEAR_NOT_SUPPORTED_FROM_API, logMessage(response))
        handleAPIError(response, Some(BAD_REQUEST))
      case _ =>
        pagerDutyLog(UNEXPECTED_RESPONSE_FROM_API, logMessage(response))
        handleAPIError(response, Some(INTERNAL_SERVER_ERROR))

    }
  }
}