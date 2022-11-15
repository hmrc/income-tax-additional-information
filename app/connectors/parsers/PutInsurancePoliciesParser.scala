

package connectors.parsers


import models.{ErrorModel, InsurancePoliciesModel}
import play.api.Logging
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.PagerDutyHelper._

object PutInsurancePoliciesParser extends APIParser with Logging{
  type PutInsurancePoliciesResponse = Either[ErrorModel, InsurancePoliciesModel]

  implicit object InsurancePoliciesHttpReads extends HttpReads[PutInsurancePoliciesResponse] {
    override def read(method: String, url: String, response: HttpResponse): PutInsurancePoliciesResponse = response.status match{
      case OK => response.json.validate[InsurancePoliciesModel].fold[PutInsurancePoliciesResponse](
        jsonErrors => badSuccessJsonFromAPI,
        parserModel => Right(parserModel)
      )
      case BAD_REQUEST =>
        pagerDutyLog(FOURXX_RESPONSE_FROM_API, logMessage(response))
        handleAPIError(response)
      case INTERNAL_SERVER_ERROR =>
        pagerDutyLog(INTERNAL_SERVER_ERROR_FROM_API, logMessage(response))
        handleAPIError(response)
      case SERVICE_UNAVAILABLE =>
        pagerDutyLog(SERVICE_UNAVAILABLE_FROM_API, logMessage(response))
        handleAPIError(response)
      case _ =>
        pagerDutyLog(UNEXPECTED_RESPONSE_FROM_API, logMessage(response))
        handleAPIError(response, Some(INTERNAL_SERVER_ERROR))

    }
  }
}