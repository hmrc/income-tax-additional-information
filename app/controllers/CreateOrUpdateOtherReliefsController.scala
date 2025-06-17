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
import models.otherReliefs.CreateOrUpdateOtherReliefsModel
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.CreateOrUpdateOtherReliefsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class CreateOrUpdateOtherReliefsController @Inject()(createOrUpdateOtherReliefsService: CreateOrUpdateOtherReliefsService,
                                                     cc: ControllerComponents,
                                                     authorisedAction: AuthorisedAction)
                                                    (implicit ec: ExecutionContext) extends BackendController(cc) {

  def createOrUpdateOtherReliefs(nino: String, taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit user =>
    user.request.body.asJson.map(_.validate[CreateOrUpdateOtherReliefsModel]) match {
      case Some(JsSuccess(model, _)) =>
        createOrUpdateOtherReliefsService.createOrUpdateOtherReliefs(nino, taxYear, model).map {
          case Right(_) => NoContent
          case Left(errorModel) => Status(errorModel.status)(Json.toJson(errorModel.toJson))
        }
      case _ => Future.successful(BadRequest)
    }
  }

}
