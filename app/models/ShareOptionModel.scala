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

package models

import play.api.libs.json.{Json, OFormat}

case class ShareOptionModel(
                             employerName: String,
                             employerRef: Option[String],
                             schemePlanType: String,
                             dateOfOptionGrant: String,
                             dateOfEvent: String,
                             optionNotExercisedButConsiderationReceived: Option[Boolean],
                             amountOfConsiderationReceived: BigDecimal,
                             noOfSharesAcquired: Int,
                             classOfSharesAcquired: Option[String],
                             exercisePrice: BigDecimal,
                             amountPaidForOption: BigDecimal,
                             marketValueOfSharesOnExcise: BigDecimal,
                             profitOnOptionExercised: BigDecimal,
                             employersNicPaid: BigDecimal,
                             taxableAmount: BigDecimal
                           )

object ShareOptionModel {
  implicit val formats: OFormat[ShareOptionModel] = Json.format[ShareOptionModel]
}