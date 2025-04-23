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

package controllers

import models.mongo.{BusinessTaxReliefs, UserAnswersModel}
import org.mongodb.scala.bson.BsonDocument
import play.api.http.Status.{BAD_REQUEST, FORBIDDEN, NO_CONTENT, OK, UNAUTHORIZED}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.{Application, Environment, Mode, inject}
import repositories.UserAnswersRepository
import support.IntegrationTest

import java.time.{Clock, Instant}


class UserAnswersControllerISpec extends IntegrationTest {

  lazy val fixedInstant: Instant = Instant.ofEpochMilli(1)

  override implicit lazy val app: Application = GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config)
    .overrides(inject.bind(classOf[Clock]).toInstance(Clock.fixed(fixedInstant, java.time.ZoneOffset.UTC)))
    .build()

  lazy val repo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  class Fixture {
    await(repo.collection.deleteMany(BsonDocument()).toFuture())
  }

  val userAnswers: UserAnswersModel = UserAnswersModel(
    mtdItId = user.mtditid,
    nino = user.nino,
    taxYear = taxYear,
    journey = BusinessTaxReliefs,
    data = Json.obj(),
    lastUpdated = fixedInstant
  )

  ".get" when {

    "user is authorised" when {

      "UserAnswers exist in Mongo for the requested User" should {

        "return OK with the expected UserAnswers as JSON" in new Fixture {

          authoriseIndividual()
          await(repo.set(userAnswers))

          val result = urlGet(routes.UserAnswersController.get(taxYear, BusinessTaxReliefs).url)

          result.status shouldBe OK
          result.json shouldBe Json.toJson(userAnswers)
        }
      }

      "No UserAnswers exist in Mongo for the requested User" should {

        "return NoContent" in new Fixture {

          authoriseIndividual()

          val result = urlGet(routes.UserAnswersController.get(taxYear, BusinessTaxReliefs).url)

          result.status shouldBe NO_CONTENT
        }
      }
    }

    "user is unauthorised" should {

      "return Unauthorised" in new Fixture {

        authoriseIndividualUnauthorized()

        val result = urlGet(routes.UserAnswersController.get(taxYear, BusinessTaxReliefs).url)

        result.status shouldBe UNAUTHORIZED

      }
    }
  }

  ".set" when {

    "user is authorised" when {

      "the UserAnswers are for the correct User (matches auth response)" when {

        "a JSON payload is supplied" when {

          "the payload is valid" when {

            "UserAnswers don't already exist" should {

              "upsert the UserAnswers" in {

                authoriseIndividual()

                val result = urlPut(routes.UserAnswersController.set().url, Json.toJson(userAnswers))

                result.status shouldBe NO_CONTENT
                await(repo.get(user.mtditid, user.nino, taxYear, BusinessTaxReliefs)) shouldBe Some(userAnswers)
              }
            }

            "UserAnswers already exist" should {

              "update the UserAnswers held" in {

                authoriseIndividual()
                await(repo.set(userAnswers))

                val updatedAnswers = userAnswers.copy(data = Json.obj("foo" -> "bar"))

                val result = urlPut(routes.UserAnswersController.set().url, Json.toJson(updatedAnswers))

                result.status shouldBe NO_CONTENT
                await(repo.get(user.mtditid, user.nino, taxYear, BusinessTaxReliefs)) shouldBe Some(updatedAnswers)
              }
            }
          }

          "the payload is invalid" should {

            "return a BadRequest" in {

              authoriseIndividual()

              val result = urlPut(routes.UserAnswersController.set().url, Json.toJson(userAnswers).as[JsObject].-("mtdItId"))

              result.status shouldBe BAD_REQUEST
              result.body should include(
                """Invalid JSON received:
                  | - (/mtdItId,List(JsonValidationError(List(error.path.missing),List())))""".stripMargin)
            }
          }
        }

        "no payload is supplied" should {

          "return a BadRequest" in {

            authoriseIndividual()

            val result = urlPut(routes.UserAnswersController.set().url, "")

            result.status shouldBe BAD_REQUEST
            result.body should include("No JSON payload received")
          }
        }
      }

      "the UserAnswers are for the wrong User (does not match auth response)" should {

        "return a Forbidden" in {

          authoriseIndividual()

          val result = urlPut(routes.UserAnswersController.set().url, Json.toJson(userAnswers.copy(mtdItId = "wrong-mtdItId")))

          result.status shouldBe FORBIDDEN
          result.body should include("Attempting to save UserAnswers for another user")
        }
      }
    }

    "user is unauthorised" should {

      "return Unauthorised" in new Fixture {

        authoriseIndividualUnauthorized()

        val result = urlPut(routes.UserAnswersController.set().url, Json.toJson(userAnswers))

        result.status shouldBe UNAUTHORIZED
      }
    }
  }

  ".delete" when {

    "user is authorised" when {

      "UserAnswers exist in Mongo for the requested User" should {

        "return NoContent with the answers deleted from Mongo" in new Fixture {

          authoriseIndividual()
          await(repo.set(userAnswers))

          val result = urlDelete(routes.UserAnswersController.delete(taxYear, BusinessTaxReliefs).url)

          result.status shouldBe NO_CONTENT
          await(repo.get(user.mtditid, user.nino, taxYear, BusinessTaxReliefs)) shouldBe None
        }
      }

      "No UserAnswers exist in Mongo for the requested User" should {

        "return NoContent" in new Fixture {

          authoriseIndividual()

          val result = urlDelete(routes.UserAnswersController.delete(taxYear, BusinessTaxReliefs).url)

          result.status shouldBe NO_CONTENT
        }
      }
    }

    "user is unauthorised" should {

      "return Unauthorised" in new Fixture {

        authoriseIndividualUnauthorized()

        val result = urlDelete(routes.UserAnswersController.delete(taxYear, BusinessTaxReliefs).url)

        result.status shouldBe UNAUTHORIZED
      }
    }
  }
}
