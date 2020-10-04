name := "telemetry-reports"

val AkkaVersion = "2.6.9"
val ZioVersion  = "1.0.1"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-csv"     % "2.0.1",
  "com.typesafe.akka"  %% "akka-actor"                  % AkkaVersion,
  "com.typesafe.akka"  %% "akka-stream"                 % AkkaVersion,
  "dev.zio"            %% "zio"                         % ZioVersion,
  "dev.zio"            %% "zio-prelude"                 % "latest.integration",
  "dev.zio"            %% "zio-interop-reactivestreams" % "1.0.3.5",
  "org.scalatest"      %% "scalatest"                   % "3.2.2"   % "test",
  "org.scalatestplus"  %% "scalacheck-1-14"             % "3.2.2.0" % "test"
)

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
