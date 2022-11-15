

package controllers

import controllers.predicates.AuthorisedAction
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.PutInsurancePoliciesService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PutInsurancePoliciesController @Inject()(putInsurancePoliciesService: PutInsurancePoliciesService,
                                               cc: ControllerComponents,
                                               authorisedAction: AuthorisedAction)
                                              (implicit ec: ExecutionContext) extends BackendController(cc) {

  def putInsurancePolicies(nino: String, taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit user =>
    putInsurancePoliciesService.putInsurancePolicies(nino, taxYear).map {
      case Right(insurancePoliciesModel) => Ok(Json.toJson(insurancePoliciesModel))
      case Left(errorModel) => Status(errorModel.status)(errorModel.toJson)
    }

  }
}