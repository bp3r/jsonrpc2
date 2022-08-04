ThisBuild / organization := "io.github.bp3r"
ThisBuild / organizationName := "bp3r"
ThisBuild / organizationHomepage := Some(
  url("https://github.com/bp3r")
)

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/bp3r/jsonrpc2"),
    "scm:git@github.bp3r/jsonrpc2.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id    = "bp3r",
    name  = "Ben",
    email = "",
    url   = url("https://github.com/bp3r")
  )
)

ThisBuild / description := "Small, simple JSONRPC2 for Scala 3."
ThisBuild / licenses := List(
  "MIT" -> new URL("https://mit-license.org")
)
ThisBuild / homepage := Some(url("https://github.com/bp3r/jsonrpc2"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }

ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

ThisBuild / publishMavenStyle := true

ThisBuild / versionScheme := Some("early-semver")
