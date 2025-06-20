import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings

val appName = "income-tax-additional-information"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.16"

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
    ),
    RoutesKeys.routesImport ++= Seq("models.mongo.Journey")
  )
  .settings(PlayKeys.playDefaultPort.withRank(KeyRanks.Invisible) := 10004)
  .settings(coverageSettings *)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .disablePlugins(JUnitXmlReportPlugin)


lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())

addCommandAlias("runAllChecks", "clean;compile;coverage;test;it/test;coverageReport;dependencyUpdates")
