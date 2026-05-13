import sbt.*

object AppDependencies {

  private val bootstrapBackendPlay30Version = "10.7.0"
  private val hmrcMongoPlayVersion          = "2.12.0"
  private val enumeratumVersion             = "1.9.6"

  private val jacksonAndPlayExclusions: Seq[InclusionRule] = Seq(
    ExclusionRule(organization = "com.fasterxml.jackson.core"),
    ExclusionRule(organization = "com.fasterxml.jackson.datatype"),
    ExclusionRule(organization = "com.fasterxml.jackson.module"),
    ExclusionRule(organization = "com.fasterxml.jackson.core:jackson-annotations"),
    ExclusionRule(organization = "com.typesafe.play")
  )

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30" % bootstrapBackendPlay30Version,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"        % hmrcMongoPlayVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.21.1",
    "com.beachape"                 %% "enumeratum"                % enumeratumVersion,
    "com.beachape"                 %% "enumeratum-play-json"      % enumeratumVersion excludeAll (jacksonAndPlayExclusions*)
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30" % bootstrapBackendPlay30Version,
    "org.jsoup"               % "jsoup"                  % "1.22.1",
    "org.scalatestplus.play" %% "scalatestplus-play"     % "7.0.2",
    "org.mockito"             % "mockito-core"           % "5.12.0",
    "org.scalatest"          %% "scalatest"              % "3.2.19",
    "org.scalatestplus"      %% "mockito-5-12"           % "3.2.19.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
