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

package connectors.parsers

import models.ErrorModel
import play.api.Logging
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object CreateOrUpdateOtherReliefsParser extends APIParser with Logging {
  type CreateOrUpdateOtherReliefsResponse = Either[ErrorModel, Boolean]

  implicit object OtherReliefsHttpReads extends HttpReads[CreateOrUpdateOtherReliefsResponse] {
    override def read(method: String, url: String, response: HttpResponse): CreateOrUpdateOtherReliefsResponse = {
      response.status match {
        case NO_CONTENT => Right(true)

        case _ =>
          logger.error(logMessage(response))
          handleAPIError(response)
      }
    }
  }
}
