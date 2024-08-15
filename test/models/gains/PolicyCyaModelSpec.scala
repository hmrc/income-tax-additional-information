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

package models.gains

import play.api.libs.json.{JsObject, Json}
import testUtils.TestSuite

import java.util.UUID

class PolicyCyaModelSpec extends TestSuite {

  val sessionId: String = UUID.randomUUID().toString
  val modelMax: PolicyCyaModel = PolicyCyaModel(
    sessionId, Some("Life Insurance"), Some("123"), Some(0), Some(""), Some(true),
    Some(0), Some(0), Some(true), Some(123.11), Some(true), Some(123.11)
  )

  val modelMin: PolicyCyaModel = PolicyCyaModel(sessionId, Some(""))

  val jsonMax: JsObject = Json.obj(
    "sessionId" -> sessionId,
    "policyType" -> "Life Insurance",
    "policyNumber" -> "123",
    "amountOfGain" -> 0,
    "policyEvent" -> "",
    "previousGain" -> true,
    "yearsPolicyHeld" -> 0,
    "yearsPolicyHeldPrevious" -> 0,
    "treatedAsTaxPaid" -> true,
    "taxPaidAmount" -> 123.11,
    "entitledToDeficiencyRelief" -> true,
    "deficiencyReliefAmount" -> 123.11
  )

  val jsonMin: JsObject = Json.obj("sessionId" -> sessionId, "policyType" -> "")

  "PolicyCyaModel" should {

    "correctly parse to Json" when {

      "the model is fully filled out" in {
        Json.toJson(modelMax) mustBe jsonMax
      }

      "the model is empty" in {
        Json.toJson(modelMin) mustBe jsonMin
      }

    }

    "correctly parse to a model" when {

      "the json contains all the data for the model" in {
        jsonMax.as[PolicyCyaModel] mustBe modelMax
      }

      "the json contains no data" in {
        jsonMin.as[PolicyCyaModel] mustBe modelMin
      }

    }

    "return true when model is full for voided isa" in {
      modelMax.copy(policyType = Some("Voided ISA")).isFinished mustBe true
    }

    "return true when model is full for not voided isa" in {
      modelMax.copy(policyType = Some("Life Insurance")).isFinished mustBe true
    }

  }

}
