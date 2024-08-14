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

package services

import models.AllGainsSessionModel
import models.gains.PolicyCyaModel
import models.mongo.MongoError
import support.IntegrationTest


class GainsSessionServiceISpec extends IntegrationTest {

  val gainsSessionServiceInvalidEncryption: GainsSessionService = appWithInvalidEncryptionKey.injector.instanceOf[GainsSessionService]

  gainsSessionService.createSessionData(AllGainsSessionModel(Seq(PolicyCyaModel(sessionId, Some(""))), gateway = Some(true)), taxYear)(user, ec)

  ".createSessionData" should {
    "return false when failing to decrypt the model" in {
      val result =
        await(gainsSessionServiceInvalidEncryption.createSessionData(
          AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true)), taxYear
        )(user, ec))
      result shouldBe Left(MongoError("User data was not updated due to mongo exception"))
    }

    "return true when successful and false when adding a duplicate" in {
      await(gainsUserDataRepository.collection.drop().toFuture())
      await(gainsUserDataRepository.ensureIndexes())

      val initialResult =
        await(gainsSessionService.createSessionData(AllGainsSessionModel(Seq(completePolicyCyaModel),
          gateway = Some(true)), taxYear)
        (user, ec))

      val duplicateResult =
        await(gainsSessionService.createSessionData(AllGainsSessionModel(Seq(completePolicyCyaModel),
          gateway = Some(true)), taxYear)
        (user, ec))

      initialResult shouldBe Right(true)
      duplicateResult shouldBe Left(MongoError("User data was not updated due to mongo exception"))
    }
  }

  ".getSessionData" should {
    "return a Gains User Data Model when calling the database" in {
      val expectedResult = await(gainsSessionService.getSessionData(taxYear)(user, ec))

      expectedResult.toOption.map(_.map(_.sessionId).contains(sessionId)) shouldBe Some(true)
      expectedResult.isRight
    }
  }

  ".updateSessionData" should {
    "return false when failing to decrypt the model" in {
      val result =
        await(gainsSessionServiceInvalidEncryption.updateSessionData(AllGainsSessionModel(Seq(completePolicyCyaModel),
          gateway = Some(true)), taxYear)
        (user, ec))

      result shouldBe Left(MongoError("User data was not updated due to mongo exception"))
    }
  }

  ".deleteSessionData" should {
    "return false when failing to decrypt the model" in {
      val result = await(gainsSessionService.deleteSessionData(taxYear)(user, ec))

      result shouldBe Right(true)
    }
  }

}
