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

package support.stubs

import config.AppConfig
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class AppConfigStub extends MockFactory {

  def config(): AppConfig = new AppConfig(mock[ServicesConfig]) {
    override lazy val desBaseUrl: String = s"http://localhost:9303"

    override lazy val environment: String = "test"
    override lazy val authorisationToken: String = "secret"
  }
}
