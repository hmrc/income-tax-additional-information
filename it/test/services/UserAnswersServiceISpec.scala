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

package services

import models.mongo.{BusinessTaxReliefs, UserAnswersModel}
import org.apache.pekko.Done
import org.mongodb.scala.bson.BsonDocument
import play.api.libs.json.Json
import repositories.UserAnswersRepository
import support.IntegrationTest


class UserAnswersServiceISpec extends IntegrationTest {

  lazy val repo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]
  lazy val service: UserAnswersService = app.injector.instanceOf[UserAnswersService]

  class Fixture {
    await(repo.collection.deleteMany(BsonDocument()).toFuture())
  }

  val userAnswers: UserAnswersModel = UserAnswersModel(
    mtdItId = user.mtditid,
    nino = user.nino,
    taxYear = taxYear,
    journey = BusinessTaxReliefs,
    data = Json.obj()
  )

  ".get" should {

    "return document from mongo for the User" in {
      await(repo.set(userAnswers))
      await(service.get(taxYear, BusinessTaxReliefs)).map(_.data) shouldBe Some(userAnswers.data)
    }
  }

  ".set" should {

    "save the data to mongo and return the document after being saved" in {

      val result = await(service.set(userAnswers))
      result.data shouldBe userAnswers.data

      await(repo.get(userAnswers.mtdItId, userAnswers.nino, taxYear, BusinessTaxReliefs)).map(_.data) shouldBe Some(userAnswers.data)
    }
  }

  ".delete" should {

    "remove the answers for the specific user and journey" in {
      await(repo.set(userAnswers))
      await(repo.get(userAnswers.mtdItId, userAnswers.nino, taxYear, BusinessTaxReliefs)).map(_.data) shouldBe Some(userAnswers.data)

      await(service.delete(taxYear, BusinessTaxReliefs)) shouldBe Done
      await(repo.get(userAnswers.mtdItId, userAnswers.nino, taxYear, BusinessTaxReliefs)) shouldBe None
    }
  }
}
