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

package connectors.parsers

import connectors.errors.{DesSingleErrorBody, DesError, DesMultiErrorsBody}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HttpResponse
import utils.PagerDutyHelper.PagerDutyKeys.{BAD_SUCCESS_JSON_FROM_DES, UNEXPECTED_RESPONSE_FROM_DES}
import utils.PagerDutyHelper.{getCorrelationId, pagerDutyLog}

trait DESParser {

  val parserName: String

  def logMessage(response: HttpResponse): String = {
    s"[$parserName][read] Received ${response.status} from DES. Body:${response.body}" + getCorrelationId(response)
  }

  def badSuccessJsonFromDES[Response]: Either[DesError, Response] = {
    pagerDutyLog(BAD_SUCCESS_JSON_FROM_DES, s"[$parserName][read] Invalid Json from DES.")
    Left(DesError(INTERNAL_SERVER_ERROR, DesSingleErrorBody.parsingError))
  }

  def handleDESError[Response](response: HttpResponse, statusOverride: Option[Int] = None): Either[DesError, Response] = {

    val status = statusOverride.getOrElse(response.status)

    try {
      val json = response.json

      lazy val desError = json.asOpt[DesSingleErrorBody]
      lazy val desErrors = json.asOpt[DesMultiErrorsBody]

      (desError, desErrors) match {
        case (Some(desError), _) => Left(DesError(status, desError))
        case (_, Some(desErrors)) => Left(DesError(status, desErrors))
        case _ =>
          pagerDutyLog(UNEXPECTED_RESPONSE_FROM_DES, s"[$parserName][read] Unexpected Json from DES.")
          Left(DesError(status, DesSingleErrorBody.parsingError))
      }
    } catch {
      case _: Exception => Left(DesError(status, DesSingleErrorBody.parsingError))
    }
  }
}
