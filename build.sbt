name := "depinj"

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "junit" % "junit" % "4.11" % "test",
  "com.h2database" % "h2" % "1.3.173"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")