name := "jsonrpc2"
version := "0.1.0"
scalaVersion := "3.1.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % "0.14.2")

libraryDependencies += "org.scalameta" %% "munit" % "1.0.0-M6" % Test
