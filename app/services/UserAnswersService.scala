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

import models.User
import models.mongo.{Journey, UserAnswersModel}
import org.apache.pekko.Done
import repositories.UserAnswersRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserAnswersService @Inject()(repo: UserAnswersRepository)(implicit ec: ExecutionContext) {

  def get(taxYear: Int, journey: Journey)(implicit user: User[_]): Future[Option[UserAnswersModel]] =
    repo.get(user.mtditid, taxYear, journey: Journey)

  def set(userAnswersModel: UserAnswersModel): Future[UserAnswersModel] =
    repo.set(userAnswersModel)

  def delete(taxYear: Int, journey: Journey)(implicit user: User[_]): Future[Done] =
    repo.delete(user.mtditid, taxYear, journey).map { _ => Done }

}
