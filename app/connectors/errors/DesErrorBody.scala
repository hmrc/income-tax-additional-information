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

package connectors.errors

import play.api.libs.json.{Json, OFormat}

sealed trait DesErrorBody

/** Single DES Error * */
case class DesSingleErrorBody(code: String, reason: String) extends DesErrorBody

/** Multiple DES Errors * */
case class DesMultiErrorsBody(failures: Seq[DesSingleErrorBody]) extends DesErrorBody

object DesSingleErrorBody {
  implicit val formats: OFormat[DesSingleErrorBody] = Json.format[DesSingleErrorBody]
  val parsingError: DesSingleErrorBody = DesSingleErrorBody("PARSING_ERROR", "Error parsing response from DES")
  val invalidView: DesSingleErrorBody = DesSingleErrorBody("INVALID_VIEW", "Submission has not passed validation. Invalid query parameter view.")
}

object DesMultiErrorsBody {
  implicit val formats: OFormat[DesMultiErrorsBody] = Json.format[DesMultiErrorsBody]
}
