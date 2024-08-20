/*
 * Copyright 2024 HM Revenue & Customs
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

import uk.gov.hmrc.DefaultBuildSettings

val appName = "income-tax-additional-information"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.12"

lazy val coverageSettings: Seq[Setting[?]] = {
  import scoverage.ScoverageKeys

  val excludedPackages = Seq(
    "<empty>",
    ".*Reverse.*",
    ".*standardError*.*",
    ".*govuk_wrapper*.*",
    ".*main_template*.*",
    "uk.gov.hmrc.BuildInfo",
    "app.*",
    "prod.*",
    "config.*",
    "testOnly.*",
    "testOnlyDoNotUseInAppConf.*",
    "controllers.testOnly.*"
  )

  Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 95,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .configs(Test)
  .settings(
    libraryDependencies ++= AppDependencies(),
    scalacOptions ++= Seq(
      "-Wconf:cat=unused-imports&src=.*routes.*:s",
      "-Wconf:cat=unused&src=.*routes.*:s"
    )
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(PlayKeys.playDefaultPort.withRank(KeyRanks.Invisible) := 10004)
  .settings(coverageSettings *)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .disablePlugins(JUnitXmlReportPlugin)


lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())

addCommandAlias("runAllChecks", "clean;compile;scalastyle;coverage;test;it/test;coverageReport")
