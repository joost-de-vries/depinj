name := "depinj"

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  "com.h2database" % "h2" % "1.4.187"
)

scalacOptions ++= Seq("-unchecked", "-feature", "-deprecation")
