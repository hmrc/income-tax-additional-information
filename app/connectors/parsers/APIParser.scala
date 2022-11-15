

package connectors.parsers

import models.{ErrorBodyModel, ErrorModel, ErrorsBodyModel}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HttpResponse
import utils.PagerDutyHelper.PagerDutyKeys.{BAD_SUCCESS_JSON_FROM_API, UNEXPECTED_RESPONSE_FROM_API}
import utils.PagerDutyHelper.{getCorrelationId, pagerDutyLog}

trait APIParser {

  def logMessage(response: HttpResponse): String = {
    s"[APIParser][read] Received ${response.status} status code. Body:${response.body}" + getCorrelationId(response)
  }

  def badSuccessJsonFromAPI[Response]: Either[ErrorModel, Response] = {
    pagerDutyLog(BAD_SUCCESS_JSON_FROM_API, s"[APIParser][read] Invalid Json response.")
    Left(ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel.parsingError))
  }

  def handleAPIError[Response](response: HttpResponse, statusOverride: Option[Int] = None): Either[ErrorModel, Response] = {

    val status = statusOverride.getOrElse(response.status)

    try {
      val json = response.json

      lazy val apiError = json.asOpt[ErrorBodyModel]
      lazy val apiErrors = json.asOpt[ErrorsBodyModel]

      (apiError, apiErrors) match {
        case (Some(apiError), _) => Left(ErrorModel(status, apiError))
        case (_, Some(apiErrors)) => Left(ErrorModel(status, apiErrors))
        case _ =>
          pagerDutyLog(UNEXPECTED_RESPONSE_FROM_API, s"[APIParser][read] Unexpected Json response.")
          Left(ErrorModel(status, ErrorBodyModel.parsingError))
      }
    } catch {
      case _: Exception => Left(ErrorModel(status, ErrorBodyModel.parsingError))
    }
  }
}