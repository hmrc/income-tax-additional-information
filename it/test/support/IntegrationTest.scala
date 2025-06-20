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

package support

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import config.AppConfig
import models.{AllGainsSessionModel, User}
import models.authorisation.SessionValues
import models.gains.{LifeInsuranceModel, PolicyCyaModel}
import models.gains.prior.GainsPriorDataModel
import models.mongo.MongoError
import org.apache.pekko.actor.ActorSystem
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{BodyWritable, WSClient, WSResponse}
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.{Application, Environment, Mode}
import repositories.GainsUserDataRepository
import services.GainsSessionService
import support.builders.UserBuilder.aUser
import support.builders.requests.AuthorisationRequestBuilder.anAuthorisationRequest
import support.helpers.{PlaySessionCookieBaker, WireMockServer}
import support.providers.TaxYearProvider
import support.stubs.WireMockStubs
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import java.util.UUID
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable, ExecutionContext, Future}

trait IntegrationTest extends AnyWordSpec
  with Matchers
  with GuiceOneServerPerSuite
  with WireMockServer
  with WireMockStubs
  with BeforeAndAfterAll
  with TaxYearProvider {

  val nino = "AA123456A"
  val mtditid = "1234567890"
  val sessionId = "sessionId-eb3158c2-0aff-4ce8-8d1b-f2208ace52fe"
  val affinityGroup = "affinityGroup"

  protected implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  protected implicit val headerCarrier: HeaderCarrier = HeaderCarrier().withExtraHeaders(headers = "mtditid" -> aUser.mtditid)
  protected implicit val actorSystem: ActorSystem = ActorSystem()
  protected implicit lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]
  protected implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val user: User[AnyContent] = new User[AnyContent](mtditid, None, nino, affinityGroup, sessionId)(FakeRequest())
  implicit val correlationId: String = UUID.randomUUID().toString
  protected lazy val appUrl = s"http://localhost:$port/update-and-submit-income-tax-return/additional-information"

  protected val config: Map[String, String] = Map(
    "defaultTaxYear" -> taxYear.toString,
    "auditing.enabled" -> "false",
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "microservice.services.income-tax-submission-frontend.url" -> "http://localhost:11111",
    "microservice.services.auth.host" -> "localhost",
    "microservice.services.auth.port" -> "11111",
    "microservice.services.income-tax-additional-information.url" -> "http://localhost:11111",
    "microservice.services.income-tax-submission.url" -> "http://localhost:11111",
    "microservice.services.view-and-change.url" -> "http://localhost:11111",
    "microservice.services.sign-in.url" -> s"/auth-login-stub/gg-sign-in",
    "useEncryption" -> "true"
  )

  def configWithInvalidEncryptionKey: Map[String, String] = Map(
    "defaultTaxYear" -> taxYear.toString,
    "auditing.enabled" -> "false",
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "microservice.services.income-tax-submission-frontend.url" -> s"http://$wiremockHost:$wiremockPort",
    "microservice.services.auth.host" -> wiremockHost,
    "microservice.services.auth.port" -> wiremockPort.toString,
    "microservice.services.income-tax-employment.url" -> s"http://$wiremockHost:$wiremockPort",
    "microservice.services.income-tax-expenses.url" -> s"http://$wiremockHost:$wiremockPort",
    "microservice.services.income-tax-submission.url" -> s"http://$wiremockHost:$wiremockPort",
    "microservice.services.view-and-change.url" -> s"http://$wiremockHost:$wiremockPort",
    "microservice.services.sign-in.url" -> s"/auth-login-stub/gg-sign-in",
    "useEncryption" -> "true",
    "mongodb.encryption.key" -> "key"
  )

  override implicit lazy val app: Application = GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config)
    .build()

  lazy val appWithInvalidEncryptionKey: Application = GuiceApplicationBuilder()
    .configure(configWithInvalidEncryptionKey)
    .build()

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWiremock()
  }

  override def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }

  lazy val defaultHeaders: Seq[(String, String)] = Seq(
    "Authorization" -> s"Bearer token",
    "mtditid" -> user.mtditid,
    "sessionId" -> user.sessionId
  )

  protected def urlGet(url: String, welsh: Boolean = false, follow: Boolean = false,
                       headers: Seq[(String, String)] = Seq())(implicit wsClient: WSClient): WSResponse = {
    val newHeaders = if (welsh) Seq(HeaderNames.ACCEPT_LANGUAGE -> "cy") ++ headers else headers
    await(wsClient.url(fullUrl(url)).withFollowRedirects(follow).withHttpHeaders(newHeaders ++ defaultHeaders: _*).get())
  }

  def urlDelete(url: String, welsh: Boolean = false, follow: Boolean = false,
                       headers: Seq[(String, String)] = Seq())(implicit wsClient: WSClient): WSResponse = {
    val newHeaders = if (welsh) Seq(HeaderNames.ACCEPT_LANGUAGE -> "cy") ++ headers else headers
    await(wsClient.url(fullUrl(url)).withFollowRedirects(follow).withHttpHeaders(newHeaders ++ defaultHeaders: _*).delete())
  }

  def urlPost[T](url: String,
                 body: T,
                 welsh: Boolean = false,
                 follow: Boolean = false,
                 headers: Seq[(String, String)] = Seq())
                (implicit wsClient: WSClient, bodyWritable: BodyWritable[T]): WSResponse = {

    val headersWithNoCheck = headers ++ Seq("Csrf-Token" -> "nocheck")
    val newHeaders = if (welsh) Seq(HeaderNames.ACCEPT_LANGUAGE -> "cy") ++ headersWithNoCheck else headersWithNoCheck
    await(wsClient.url(fullUrl(url)).withFollowRedirects(follow).withHttpHeaders(newHeaders ++ defaultHeaders: _*).post(body))
  }

  def urlPut[T](url: String,
                body: T,
                welsh: Boolean = false,
                follow: Boolean = false,
                headers: Seq[(String, String)] = Seq())
               (implicit wsClient: WSClient, bodyWritable: BodyWritable[T]): WSResponse = {

    val headersWithNoCheck = headers ++ Seq("Csrf-Token" -> "nocheck")
    val newHeaders = if (welsh) Seq(HeaderNames.ACCEPT_LANGUAGE -> "cy") ++ headersWithNoCheck else headersWithNoCheck
    await(wsClient.url(fullUrl(url)).withFollowRedirects(follow).withHttpHeaders(newHeaders ++ defaultHeaders: _*).put(body))
  }

  private def fullUrl(endOfUrl: String): String = s"http://localhost:$port" + endOfUrl

  protected def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

  protected def status(awaitable: Future[Result]): Int = await(awaitable).header.status

  protected def playSessionCookies(taxYear: Int,
                                   validTaxYears: Seq[Int] = validTaxYearList,
                                   extraData: Map[String, String] = Map.empty): String = PlaySessionCookieBaker.bakeSessionCookie(Map(
    SessionValues.TAX_YEAR -> taxYear.toString,
    SessionValues.VALID_TAX_YEARS -> validTaxYears.mkString(","),
    SessionKeys.sessionId -> aUser.sessionId,
    SessionValues.CLIENT_NINO -> aUser.nino,
    SessionValues.CLIENT_MTDITID -> aUser.mtditid,
    SessionKeys.authToken -> "mock-bearer-token"
  ) ++ extraData)

  val completePolicyCyaModel: PolicyCyaModel = PolicyCyaModel(
    sessionId, Some("Life Insurance"), Some("123"), Some(0), Some(""), Some(true), Some(0), Some(0), Some(true), Some(123.11), Some(true), Some(123.11)
  )

  val gainsPriorDataModel: Option[GainsPriorDataModel] =
    Some(
      GainsPriorDataModel(
        "submittedOn",
        lifeInsurance = Some(Seq(LifeInsuranceModel(Some("abc123"), Some("event"), BigDecimal(123.45), Some(true), Some(5), Some(10)))),
        None,
        None,
        None,
        None
      )
    )

  val gainsUserDataRepository: GainsUserDataRepository = app.injector.instanceOf[GainsUserDataRepository]

  val gainsSessionService: GainsSessionService = new GainsSessionService(gainsUserDataRepository)(correlationId)

  def populateSessionData(): Either[MongoError, Unit] =
    await(gainsSessionService.createSessionData(AllGainsSessionModel(Seq(PolicyCyaModel(sessionId, Some("Life Insurance"), Some("RefNo13254687"), Some(123.11),
      Some("Full or part surrender"), Some(true), Some(99), Some(10), Some(true), None, Some(true), Some(100))),
      gateway = Some(true)), taxYear)(user, ec))

  def populateSessionDataWithRandomSession(): Either[MongoError, Unit] =
    await(gainsSessionService.createSessionData(AllGainsSessionModel(Seq(PolicyCyaModel(UUID.randomUUID().toString, Some(""))), gateway = Some(true)), taxYear)
    (anAuthorisationRequest.user, ec))

  def populateOnlyGatewayData(): Either[MongoError, Unit] =
    await(gainsSessionService.createSessionData(AllGainsSessionModel(Seq[PolicyCyaModel]().empty, gateway = Some(true)), taxYear)(user, ec))

  def populateSessionDataWithEmptyGateway(): Either[MongoError, Unit] =
    await(gainsSessionService.createSessionData(AllGainsSessionModel(Seq(), gateway = None), taxYear)(user, ec))

  def populateSessionDataWithFalseGateway(): Either[MongoError, Unit] =
    await(gainsSessionService.createSessionData(AllGainsSessionModel(Seq(), gateway = Some(false)), taxYear)(user, ec))

  def populateWithSessionDataModel(cya: Seq[PolicyCyaModel]): Either[MongoError, Unit] =
    await(gainsSessionService.createSessionData(AllGainsSessionModel(cya, gateway = Some(true)), taxYear)(user, ec))

  def clearSession(): Boolean = await(gainsUserDataRepository.clear(taxYear))

  def userDataStub(userData: Option[GainsPriorDataModel], nino: String, taxYear: Int): StubMapping = {
    stubGetWithHeadersCheck(
      s"/income-tax-additional-information/income-tax/insurance-policies/income/$nino/$taxYear", OK,
      Json.toJson(userData).toString(), "X-Session-ID" -> sessionId, "mtditid" -> mtditid)
  }

  def emptyUserDataStub(nino: String = nino, taxYear: Int = taxYear): StubMapping = {
    stubGetWithHeadersCheck(
      s"/income-tax-additional-information/income-tax/insurance-policies/income/$nino/$taxYear", NOT_FOUND,
      "", "X-Session-ID" -> sessionId, "mtditid" -> mtditid)
  }

}
