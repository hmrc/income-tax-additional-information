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

package repositories

import config.AppConfig
import org.mongodb.scala.model.Indexes.{ascending, compoundIndex}
import org.mongodb.scala.model.{IndexModel, IndexOptions}

import java.util.concurrent.TimeUnit

object UserAnswersIndexes {

  def indexes()(implicit appConfig: AppConfig): Seq[IndexModel] = Seq(
    IndexModel(
      compoundIndex(
        ascending("mtdItId"),
        ascending("nino"),
        ascending("journey"),
        ascending("taxYear")
      ),
      IndexOptions().name("mtdItId-nino-journey-taxYear-index")
    ),
    IndexModel(
      ascending("lastUpdated"),
      IndexOptions()
        .expireAfter(appConfig.mongoTTL, TimeUnit.DAYS)
        .name("last-updated-index")
    )
  )

}