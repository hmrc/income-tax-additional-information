

package services

import connectors.PutInsurancePoliciesConnector
import connectors.parsers.PutInsurancePoliciesParser.PutInsurancePoliciesResponse
import models._
import testUtils.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class PutInsurancePoliciesServiceSpec extends TestSuite {

  val connector: PutInsurancePoliciesConnector = mock[PutInsurancePoliciesConnector]
  val service: PutInsurancePoliciesService = new PutInsurancePoliciesService(connector)

  ".putInsurancePolicies" should {

    "return the connector response" in {

      val expectedResult: PutInsurancePoliciesResponse = Right(InsurancePoliciesModel(
        submittedOn = "2020-01-04T05:01:01Z",
        lifeInsurance = Seq(LifeInsuranceModel(Some("RefNo13254687"), Some("Life"), 123.45, Some(true), Some(4), Some(3), Some(123.45))),
        capitalRedemption = Seq(CapitalRedemptionModel(Some("RefNo13254687"), Some("Capital"), 123.45, Some(true), Some(3), Some(2), Some(0))),
        lifeAnnuity = Seq(LifeAnnuityModel(Some("RefNo13254687"), Some("Life"), 0, Some(true), Some(2), Some(22), Some(123.45))),
        voidedIsa = Seq(VoidedIsaModel(Some("RefNo13254687"), Some("isa"), 123.45, Some(123.45), Some(5), Some(6))),
        foreign = Seq(ForeignModel(Some("RefNo13254687"), 123.45, Some(123.45), Some(3)))
      ))

      (connector.putInsurancePolicies(_: String, _: Int)(_: HeaderCarrier))
        .expects("12345678", 1234, *)
        .returning(Future.successful(expectedResult))

      val result = await(service.putInsurancePolicies("12345678", 1234))

      result mustBe expectedResult

    }
  }
}