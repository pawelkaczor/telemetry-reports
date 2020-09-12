name := "statistics"

val AkkaVersion = "2.6.9"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "2.0.1",
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "org.scala-lang.modules" %% "scala-collection-contrib" % "0.2.1"
)