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

import com.mongodb.client.model
import config.AppConfig
import models.mongo.{Journey, UserAnswersModel}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.{and, equal}
import org.mongodb.scala.model._
import org.mongodb.scala.result.DeleteResult
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs.toBson
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.play.http.logging.Mdc
import utils.TimeMachine

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserAnswersRepository @Inject()(mongoComponent: MongoComponent,
                                      appConfig: AppConfig,
                                      timeMachine: TimeMachine)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[UserAnswersModel](
    collectionName = "userAnswers",
    mongoComponent = mongoComponent,
    domainFormat   = UserAnswersModel.format,
    indexes = UserAnswersIndexes.indexes()(appConfig),
    replaceIndexes = appConfig.userAnswersReplaceIndexes
  ) {

  private def pk(mtdItId: String, nino: String, taxYear: Int, journey: Journey): Bson = and(
    equal("mtdItId", toBson(mtdItId)),
    equal("nino", toBson(nino)),
    equal("taxYear", toBson(taxYear)),
    equal("journey", toBson(journey))
  )

  def get(mtdItId: String, nino: String, taxYear: Int, journey: Journey): Future[Option[UserAnswersModel]] =
    Mdc.preservingMdc(
      collection
        .findOneAndUpdate(
          filter = pk(mtdItId, nino, taxYear, journey),
          update = Updates.set("lastUpdated", timeMachine.instantNow),
          options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )
        .headOption()
    )

  def set(userData: UserAnswersModel): Future[UserAnswersModel] =
    Mdc.preservingMdc(
      collection
        .findOneAndReplace(
          filter = pk(userData.mtdItId, userData.nino, userData.taxYear, userData.journey),
          replacement = userData.copy(lastUpdated = timeMachine.instantNow),
          options = FindOneAndReplaceOptions()
            .upsert(true)
            .returnDocument(model.ReturnDocument.AFTER)
        )
        .toFuture()
    )

  def delete(mtdItId: String, nino: String, taxYear: Int, journey: Journey): Future[DeleteResult] =
    Mdc.preservingMdc(
      collection
        .deleteOne(pk(mtdItId, nino, taxYear, journey))
        .toFuture()
    )
}

