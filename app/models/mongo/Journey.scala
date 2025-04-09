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

package models.mongo

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue}

sealed trait Journey { val name: String }

object BusinessTaxReliefs extends Journey { override val name: String = "businessTaxReliefs" }

object Journey {
  implicit val format: Format[Journey] = new Format[Journey] {
    override def writes(model: Journey): JsValue = JsString(model.name)

    override def reads(json: JsValue): JsResult[Journey] = json match {
      case JsString(BusinessTaxReliefs.name) => JsSuccess(BusinessTaxReliefs)
      case _ => JsError("Unknown journey")
    }
  }
}
