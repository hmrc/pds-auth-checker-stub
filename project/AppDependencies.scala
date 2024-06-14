import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.5.0"

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapVersion,
    "com.beachape" %% "enumeratum" % "1.7.3",
    "com.beachape" %% "enumeratum-play-json" % "1.8.0"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.scalatestplus" %% "scalacheck-1-17" % "3.2.18.0"
  ).map(_ % Test)

  val it = Seq.empty
}
