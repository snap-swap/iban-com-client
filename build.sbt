name := "iban-com-client"

organization := "com.snapswap"

version := "1.0.1"

scalaVersion := "2.11.8"

scalacOptions := Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import",
  "-encoding",
  "UTF-8"
)

libraryDependencies ++= {
  val akkaV = "2.4.11"
  Seq(
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
    "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test"
  )
}
