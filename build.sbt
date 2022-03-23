import Dependencies._

ThisBuild / organization := "uz"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version      := "1.0"

lazy val root = (project in file("."))
  .settings(
    name := "soccer"
  )
  .aggregate(server, tests)

lazy val server = (project in file("modules/server"))
  .settings(
    name := "soccer",
    libraryDependencies ++= coreLibraries,
    scalacOptions ++= Seq(
      "-Ymacro-annotations",
      "-encoding",
      "utf8",             // Option and arguments on same line
      "-Xfatal-warnings", // New lines for each options
      "-deprecation",
      "-unchecked",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-language:existentials",
      "-language:postfixOps"
    ),
    coverageEnabled := true
  )

lazy val tests = project
  .in(file("modules/tests"))
  .configs(IntegrationTest)
  .settings(
    name := "soccer-test-suite",
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    Defaults.itSettings,
    libraryDependencies ++= testLibraries
  )
  .dependsOn(server)