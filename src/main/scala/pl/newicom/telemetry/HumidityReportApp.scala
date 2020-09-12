package pl.newicom.telemetry

import java.io.File

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}

object HumidityReportApp extends App {
  if (args.length == 0) {
    throw new IllegalArgumentException("path to csv directory is missing")
  }

  implicit val system: ActorSystem  = ActorSystem("telemetry", ConfigFactory.load())
  implicit val ec: ExecutionContext = system.dispatcher

  val report: HumidityReport = Await.result(HumidityReportCreator.createHumidityReport(new File(args(0))), 1.day)

  println(report)
}
