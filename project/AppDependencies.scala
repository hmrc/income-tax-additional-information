/*
 * Copyright 2022 HM Revenue & Customs
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

import AppDependencies.jacksonAndPlayExclusions
import sbt.*


object AppDependencies {

  private val bootstrapBackendPlay30Version = "8.5.0"
  private val hmrcMongoPlayVersion = "1.8.0"

  private val jacksonAndPlayExclusions: Seq[InclusionRule] = Seq(
    ExclusionRule(organization = "com.fasterxml.jackson.core"),
    ExclusionRule(organization = "com.fasterxml.jackson.datatype"),
    ExclusionRule(organization = "com.fasterxml.jackson.module"),
    ExclusionRule(organization = "com.fasterxml.jackson.core:jackson-annotations"),
    ExclusionRule(organization = "com.typesafe.play")
  )

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-backend-play-30"              % bootstrapBackendPlay30Version,
    "uk.gov.hmrc.mongo"             %% "hmrc-mongo-play-30"         % hmrcMongoPlayVersion,
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"                   % "2.17.0",
    "com.beachape"                  %% "enumeratum"                             % "1.7.3",
    "com.beachape"                  %% "enumeratum-play-json"                   % "1.7.3" excludeAll (jacksonAndPlayExclusions *)
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapBackendPlay30Version % Test,
    "org.scalamock"           %% "scalamock"                  % "5.2.0"                       % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
