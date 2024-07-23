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

package config

import com.google.inject.ImplementedBy

import javax.inject.Inject
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration.Duration


@ImplementedBy(classOf[BackendAppConfig])
trait AppConfig {
  val authBaseUrl: String
  val auditingEnabled: Boolean
  val graphiteHost: String

  val ifAuthorisationToken: String
  val ifBaseUrl: String
  val ifEnvironment: String

  val addInfoFEBaseUrl: String

  val useEncryption: Boolean
  val encryptionKey: String
  val mongoTTL: Int

  def authorisationTokenFor(apiVersion: String): String
}


class BackendAppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) extends AppConfig {

  val authBaseUrl: String = servicesConfig.baseUrl("auth")

  lazy val useEncryption: Boolean = servicesConfig.getBoolean("useEncryption")

  // Mongo config
  lazy val encryptionKey: String = servicesConfig.getString("mongodb.encryption.key")
  lazy val mongoTTL: Int = Duration(servicesConfig.getString("mongodb.timeToLive")).toDays.toInt

  val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")
  val graphiteHost: String = config.get[String]("microservice.metrics.graphite.host")

  lazy val ifAuthorisationToken: String = "microservice.services.integration-framework.authorisation-token"
  lazy val ifBaseUrl: String = servicesConfig.baseUrl(serviceName = "integration-framework")
  lazy val ifEnvironment: String = servicesConfig.getString(key = "microservice.services.integration-framework.environment")

  val addInfoFEBaseUrl: String = config.get[String]("microservice.services.income-tax-additional-information-frontend.url")

  def authorisationTokenFor(api: String): String = config.get[String](s"microservice.services.integration-framework.authorisation-token.$api")
}