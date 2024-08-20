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

package support.builders

import models.User
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup

object UserBuilder {

  val aUser: User[AnyContent] = User[AnyContent](
    mtditid = "1234567890",
    arn = None,
    nino = "AA123456A",
    sessionId = "sessionId-eb3158c2-0aff-4ce8-8d1b-f2208ace52fe",
    affinityGroup = AffinityGroup.Individual.toString
  )(FakeRequest())

  val anAgentUser: User[AnyContent] = aUser.copy(arn = Some("0987654321"), affinityGroup = AffinityGroup.Agent.toString)(FakeRequest())
}
