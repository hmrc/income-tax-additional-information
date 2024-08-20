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

package controllers

import controllers.predicates.AuthorisedAction
import models.AllGainsSessionModel
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.GainsSessionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class CreateGainsSessionDataController @Inject()(gainsSessionService: GainsSessionService,
                                                 cc: ControllerComponents,
                                                 authorisedAction: AuthorisedAction)
                                                (implicit ec: ExecutionContext) extends BackendController(cc) {

  def createSession(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit user =>
    user.body.asJson.map(_.validate[AllGainsSessionModel]) match {
      case Some(JsSuccess(model, _)) =>
        gainsSessionService.createSessionData(model, taxYear).map {
          case Right(_) => NoContent
          case Left(error) => Status(INTERNAL_SERVER_ERROR)(Json.toJson(error.message))
        }
      case _ =>
        Future.successful(BadRequest)
    }
  }
}
