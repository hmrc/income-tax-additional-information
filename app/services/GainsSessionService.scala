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

import models.mongo.{DataNotDeleted, DatabaseError, GainsUserDataModel, MongoError}
import models.{AllGainsSessionModel, User}
import play.api.Logging
import repositories.GainsUserDataRepository

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GainsSessionService @Inject()(gainsUserDataRepository: GainsUserDataRepository)(implicit correlationId: String) extends Logging {

  def createSessionData[A](cyaModel: AllGainsSessionModel, taxYear: Int)
                          (implicit user: User[_], ec: ExecutionContext): Future[Either[MongoError, Unit]] = {

    val userData = GainsUserDataModel(user.sessionId, user.mtditid, user.nino, taxYear, Some(cyaModel), Instant.now)

    gainsUserDataRepository.create(userData).map {
      case Right(_) =>
        Right(())
      case Left(databaseError) =>
        logger.error(s"[GainsSessionService][createSessionData] session create failed. correlation id: " + correlationId)
        Left(MongoError(databaseError.message))
    }
  }

  def getSessionData(taxYear: Int)(implicit user: User[_], ec: ExecutionContext): Future[Either[DatabaseError, Option[GainsUserDataModel]]] = {
    gainsUserDataRepository.find(taxYear).map {
      case Right(userData) =>
        Right(userData)
      case Left(databaseError) =>
        logger.error("[GainsSessionService][getSessionData] Could not find user session. correlation id: " + correlationId)
        Left(MongoError(databaseError.message))
    }
  }

  def updateSessionData[A](cyaModel: AllGainsSessionModel, taxYear: Int)
                          (implicit user: User[_], ec: ExecutionContext): Future[Either[DatabaseError, Unit]] = {

    val userData = GainsUserDataModel(user.sessionId, user.mtditid, user.nino, taxYear, Some(cyaModel), Instant.now)

    gainsUserDataRepository.update(userData).map {
      case Right(_) => Right(())
      case Left(databaseError) =>
        logger.error(s"[GainsSessionService][updateSessionData] session update failure. correlation id: " + correlationId)
        Left(MongoError(databaseError.message))
    }
  }

  def deleteSessionData[A](taxYear: Int)(implicit user: User[_], ec: ExecutionContext): Future[Either[DatabaseError, Boolean]]  = {
    gainsUserDataRepository.clear(taxYear)(user).map {
      case true =>
        Right(true)
      case _ =>
        logger.error(s"[GainsSessionService][deleteSessionData] session delete failure. correlation id: " + correlationId)
        Left(MongoError(DataNotDeleted.message))
    }
  }

}
