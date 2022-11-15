
package controllers

import connectors.parsers.PutInsurancePoliciesParser.PutInsurancePoliciesResponse
import models._
import org.scalamock.handlers.CallHandler3
import play.api.http.Status._
import play.api.libs.json.Json
import services.PutInsurancePoliciesService
import testUtils.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class PutInsurancePoliciesControllerSpec extends TestSuite {

  val serviceMock: PutInsurancePoliciesService = mock[PutInsurancePoliciesService]
  val controller = new PutInsurancePoliciesController(serviceMock, mockControllerComponents, authorisedAction)

  val notFoundModel: ErrorModel = ErrorModel(NOT_FOUND, ErrorBodyModel("NotFound", "Unable to find source"))
  val serviceUnavailableModel: ErrorModel =
    ErrorModel(SERVICE_UNAVAILABLE, ErrorBodyModel("SERVICE_UNAVAILABLE", "The service is currently unavailable"))
  val badRequestModel: ErrorModel = ErrorModel(BAD_REQUEST, ErrorBodyModel("BAD_REQUEST", "The supplied NINO is invalid"))
  val internalServerErrorModel: ErrorModel =
    ErrorModel(INTERNAL_SERVER_ERROR, ErrorBodyModel("INTERNAL_SERVER_ERROR", "There has been an unexpected error"))


  val nino = "nino"
  val taxYear = 2023
  val mtditid = "someMtditid"

  val insurancePoliciesModel: InsurancePoliciesModel = InsurancePoliciesModel(
    submittedOn = "2020-01-04T05:01:01Z",
    lifeInsurance = Seq(LifeInsuranceModel(Some("RefNo13254687"), Some("Life"), 123.45, Some(true), Some(4), Some(3), Some(123.45))),
    capitalRedemption = Seq(CapitalRedemptionModel(Some("RefNo13254687"), Some("Capital"), 123.45, Some(true), Some(3), Some(2), Some(0))),
    lifeAnnuity = Seq(LifeAnnuityModel(Some("RefNo13254687"), Some("Life"), 0, Some(true), Some(2), Some(22), Some(123.45))),
    voidedIsa = Seq(VoidedIsaModel(Some("RefNo13254687"), Some("isa"), 123.45, Some(123.45), Some(5), Some(6))),
    foreign = Seq(ForeignModel(Some("RefNo13254687"), 123.45, Some(123.45), Some(3)))
  )

  ".putInsurancePolicies" should {

    "Return a 200 OK response with valid InsurancePolicies" in {

      val serviceResult = Right(insurancePoliciesModel)
      val finalResult = Json.toJson(insurancePoliciesModel).toString()

      def serviceCallMock(): CallHandler3[String, Int, HeaderCarrier, Future[PutInsurancePoliciesResponse]] =
        (serviceMock.putInsurancePolicies(_: String, _: Int)(_: HeaderCarrier))
          .expects(nino, taxYear, *)
          .returning(Future.successful(serviceResult))


      val result = {
        mockAuth()
        serviceCallMock()
        controller.putInsurancePolicies(nino, taxYear)(fakeRequest)
      }

      status(result) mustBe OK
      bodyOf(result) mustBe finalResult

    }

    "return a Left response" when {

      def mockGetSavingIncomeDataWithError(errorModel: ErrorModel): CallHandler3[String, Int, HeaderCarrier, Future[PutInsurancePoliciesResponse]] = {
        (serviceMock.putInsurancePolicies(_: String, _: Int)(_: HeaderCarrier))
          .expects(nino, taxYear, *)
          .returning(Future.successful(Left(errorModel)))
      }

      "the service returns a NO_CONTENT" in {
        val result = {
          mockAuth()
          mockGetSavingIncomeDataWithError(notFoundModel)
          controller.putInsurancePolicies(nino, taxYear)(fakeRequest)
        }
        status(result) mustBe NOT_FOUND
      }

      "the service returns a SERVICE_UNAVAILABLE" in {
        val result = {
          mockAuth()
          mockGetSavingIncomeDataWithError(serviceUnavailableModel)
          controller.putInsurancePolicies(nino, taxYear)(fakeRequest)
        }
        status(result) mustBe SERVICE_UNAVAILABLE
      }
      "the service returns a BAD_REQUEST" in {
        val result = {
          mockAuth()
          mockGetSavingIncomeDataWithError(badRequestModel)
          controller.putInsurancePolicies(nino, taxYear)(fakeRequest)
        }
        status(result) mustBe BAD_REQUEST
      }
      "the service returns a INTERNAL_SERVER_ERROR" in {
        val result = {
          mockAuth()
          mockGetSavingIncomeDataWithError(internalServerErrorModel)
          controller.putInsurancePolicies(nino, taxYear)(fakeRequest)
        }
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}