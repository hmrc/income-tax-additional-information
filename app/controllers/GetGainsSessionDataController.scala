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
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.GainsSessionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class GetGainsSessionDataController @Inject()(gainsSessionService: GainsSessionService,
                                              cc: ControllerComponents,
                                              authorisedAction: AuthorisedAction)
                                             (implicit ec: ExecutionContext) extends BackendController(cc) {

  def getSession(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit user =>
    gainsSessionService.getSessionData(taxYear).map {
      case Right(optionalUserData) => optionalUserData match {
        case Some(userData) => Ok(Json.toJson(userData))
        case _ => NoContent
      }
      case Left(error) =>
        NotFound(Json.toJson(error.message))
    }
  }
}
