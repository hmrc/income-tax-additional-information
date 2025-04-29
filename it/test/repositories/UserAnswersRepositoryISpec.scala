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

package repositories

import models.User
import models.mongo._
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.DeleteResult
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContent
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import support.IntegrationTest

class UserAnswersRepositoryISpec extends IntegrationTest with FutureAwaits with DefaultAwaitTimeout {

  val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]
  val testUser: User[AnyContent] = User(mtditid, None, nino, "individual", sessionId)

  private def count: Long = await(userAnswersRepo.collection.countDocuments().toFuture())

  class Fixture {
    await(userAnswersRepo.collection.deleteMany(BsonDocument()).toFuture())
    count mustBe 0
  }

  val userAnswers: UserAnswersModel = UserAnswersModel(
    mtdItId = mtditid,
    nino = nino,
    taxYear = taxYear,
    journey = BusinessTaxReliefs,
    data = JsObject.empty
  )

  implicit val request: FakeRequest[AnyContent] = FakeRequest()

  ".set" when {

    "document does NOT exist" should {

      "add a document to the collection" in new Fixture {
        val upserted: UserAnswersModel = await(userAnswersRepo.set(userAnswers))
        count mustBe 1
        upserted.data mustBe Json.obj()
      }
    }

    "document does exist" should {

      "update the document in the collection" in new Fixture {
        val upserted: UserAnswersModel = await(userAnswersRepo.set(userAnswers))
        count mustBe 1
        upserted.data mustBe Json.obj()
        val updated: UserAnswersModel = await(userAnswersRepo.set(userAnswers.copy(data = Json.obj("foo" -> "bar"))))
        updated.data mustBe Json.obj("foo" -> "bar")
      }
    }
  }

  ".get" should {

    "return None when no document exists" in new Fixture {
      await(userAnswersRepo.get(userAnswers.mtdItId, userAnswers.taxYear, userAnswers.journey)) mustBe None
    }

    "return Some(UserAnswers) when document exists" in new Fixture {
      await(userAnswersRepo.set(userAnswers))
      count mustBe 1
      val result: Option[UserAnswersModel] = await(userAnswersRepo.get(userAnswers.mtdItId, userAnswers.taxYear, userAnswers.journey))
      result.map(_.data) mustBe Some(Json.obj())
    }
  }

  ".delete" should {

    "clear the document for the current user" in new Fixture {
      await(userAnswersRepo.set(userAnswers))
      await(userAnswersRepo.set(userAnswers.copy(mtdItId = "other")))
      await(userAnswersRepo.set(userAnswers.copy(mtdItId = "other2")))
      count shouldBe 3
      val deleted: DeleteResult = await(userAnswersRepo.delete(userAnswers.mtdItId, userAnswers.taxYear, userAnswers.journey))
      deleted.getDeletedCount mustBe 1
      count shouldBe 2
    }
  }
}
