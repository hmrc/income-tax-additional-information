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

import controllers.predicates.AuthorisedAction
import models.User
import models.mongo.{Journey, UserAnswersModel}
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserAnswersController @Inject()(userAnswersService: UserAnswersService,
                                      auth: AuthorisedAction,
                                      cc: ControllerComponents)
                                     (implicit ec: ExecutionContext) extends BackendController(cc) {

  def get(taxYear: Int, journey: Journey): Action[AnyContent] = auth.async { implicit user =>
    userAnswersService.get(taxYear, journey).map {
      case Some(answers) => Ok(Json.toJson(answers))
      case _ => NoContent
    }
  }

  def set(): Action[AnyContent] = auth.async { implicit user =>
    user.request.body.asJson.map(_.validate[UserAnswersModel]) match {
      case Some(JsSuccess(answers, _)) =>
        checkUser(answers) {
          userAnswersService.set(answers).map(_ => NoContent)
        }
      case Some(JsError(errs)) =>
        Future(BadRequest(s"Invalid JSON received:\n - ${errs.mkString("\n - ")}"))
      case _ =>
        Future(BadRequest("No JSON payload received"))
    }
  }

  def delete(taxYear: Int, journey: Journey): Action[AnyContent] = auth.async { implicit user =>
    userAnswersService.delete(taxYear, journey).map(_ => NoContent)
  }

  private def checkUser(userAnswers: UserAnswersModel)(f: => Future[Result])(implicit user: User[_]): Future[Result] =
    if (userAnswers.mtdItId == user.mtditid && userAnswers.nino == user.nino) f else {
      Future(Forbidden("Attempting to save UserAnswers for another user"))
    }
}

