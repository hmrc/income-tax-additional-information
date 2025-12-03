import sbt.*


object AppDependencies {

  private val bootstrapBackendPlay30Version = "10.4.0"
  private val hmrcMongoPlayVersion = "2.11.0"

  private val jacksonAndPlayExclusions: Seq[InclusionRule] = Seq(
    ExclusionRule(organization = "com.fasterxml.jackson.core"),
    ExclusionRule(organization = "com.fasterxml.jackson.datatype"),
    ExclusionRule(organization = "com.fasterxml.jackson.module"),
    ExclusionRule(organization = "com.fasterxml.jackson.core:jackson-annotations"),
    ExclusionRule(organization = "com.typesafe.play")
  )

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-backend-play-30"  % bootstrapBackendPlay30Version,
    "uk.gov.hmrc.mongo"             %% "hmrc-mongo-play-30"         % hmrcMongoPlayVersion,
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"       % "2.20.1",
    "com.beachape"                  %% "enumeratum"                 % "1.9.0",
    "com.beachape"                  %% "enumeratum-play-json"       % "1.9.0" excludeAll (jacksonAndPlayExclusions *)
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapBackendPlay30Version,
    "org.scalamock"           %% "scalamock"                  % "7.5.2"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
