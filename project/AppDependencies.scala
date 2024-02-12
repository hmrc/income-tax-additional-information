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

import play.core.PlayVersion.current
import sbt._


object AppDependencies {

  private val bootstrapBackendPlay28Version = "8.4.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-backend-play-28"  % bootstrapBackendPlay28Version,
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"       % "2.13.3"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % bootstrapBackendPlay28Version % "test, it",
    "com.typesafe.play"       %% "play-test"                  % current                       % Test,
    "org.scalamock"           %% "scalamock"                  % "5.2.0"                       % Test,
    "org.scalatest"           %% "scalatest"                  % "3.2.13"                      % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.62.2"                      % "test, it",
    "com.github.tomakehurst"  %  "wiremock-jre8"              % "2.33.2"                      % "test, it"
  )
}
