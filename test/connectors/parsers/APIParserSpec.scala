
package connectors.httpParsers


import connectors.parsers.APIParser
import models.{ErrorBodyModel, ErrorModel, ErrorsBodyModel}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsValue, Json}
import testUtils.TestSuite
import uk.gov.hmrc.http.HttpResponse

class APIParserSpec extends TestSuite {

  object FakeParser extends APIParser {
  }

  def httpResponse(json: JsValue =
                   Json.parse(
                     """{"failures":[
                       |{"code":"SERVICE_UNAVAILABLE","reason":"The service is currently unavailable"},
                       |{"code":"INTERNAL_SERVER_ERROR","reason":"The service is currently facing issues."}]}""".stripMargin)): HttpResponse = HttpResponse(
    INTERNAL_SERVER_ERROR,
    json,
    Map("CorrelationId" -> Seq("1234645654645"))
  )

  "FakeParser" should {
    "log the correct message" in {
      val result = FakeParser.logMessage(httpResponse())
      result mustBe (
        """[APIParser][read] Received 500 status code. Body:{
          |  "failures" : [ {
          |    "code" : "SERVICE_UNAVAILABLE",
          |    "reason" : "The service is currently unavailable"
          |  }, {
          |    "code" : "INTERNAL_SERVER_ERROR",
          |    "reason" : "The service is currently facing issues."
          |  } ]
          |} CorrelationId: 1234645654645""".stripMargin)
    }
    "return the the correct error" in {
      val result = FakeParser.badSuccessJsonFromAPI
      result mustBe Left(ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel("PARSING_ERROR", "Error parsing response from API")))
    }
    "handle multiple errors" in {
      val result = FakeParser.handleAPIError(httpResponse())
      result mustBe Left(ErrorModel(INTERNAL_SERVER_ERROR, ErrorsBodyModel(Seq(
        ErrorBodyModel("SERVICE_UNAVAILABLE", "The service is currently unavailable"),
        ErrorBodyModel("INTERNAL_SERVER_ERROR", "The service is currently facing issues.")
      ))))
    }
    "handle single errors" in {
      val result = FakeParser.handleAPIError(httpResponse(Json.parse(
        """{"code":"INTERNAL_SERVER_ERROR","reason":"The service is currently facing issues."}""".stripMargin)))
      result mustBe Left(ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel("INTERNAL_SERVER_ERROR", "The service is currently facing issues.")))
    }
  }

}