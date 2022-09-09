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

package utils

import support.UnitTest
import uk.gov.hmrc.http.HttpResponse

class PagerDutyHelperSpec extends UnitTest {

  private val status = 200

  "PagerDutyHelper" should {
    "return string containing correlationId when response contains correlationId" in {
      val result = PagerDutyHelper.getCorrelationId(HttpResponse(status, "", Map("CorrelationId" -> Seq("some_correlation_id"))))
      result shouldBe " CorrelationId: some_correlation_id"
    }

    "return empty string when response does not contain correlationId" in {
      val result = PagerDutyHelper.getCorrelationId(HttpResponse(status, ""))
      result shouldBe ""
    }
  }
}
