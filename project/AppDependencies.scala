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

  private val bootstrapBackendPlay30Version = "8.5.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-backend-play-30"  % bootstrapBackendPlay30Version,
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"       % "2.16.1"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapBackendPlay30Version % "test",
    "org.scalamock"           %% "scalamock"                  % "5.2.0"                       % Test,
    "org.scalatest"           %% "scalatest"                  % "3.2.17"                      % Test,
    "com.vladsch.flexmark"     %  "flexmark-all"              % "0.64.8"                    % "test",
    "com.github.tomakehurst"  %  "wiremock-jre8"              % "2.35.1"                      % "test"
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
