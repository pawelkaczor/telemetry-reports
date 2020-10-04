package pl.newicom.telemetry

import java.io.File

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import pl.newicom.telemetry.zio.ReportCreator

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}
import scala.math.BigDecimal.RoundingMode

object HumidityReportApp extends App {
  if (args.length == 0) {
    throw new IllegalArgumentException("path to csv directory is missing")
  }

  implicit val system: ActorSystem  = ActorSystem("telemetry", ConfigFactory.load())
  implicit val ec: ExecutionContext = system.dispatcher

  val report: HumidityReport = Await.result(ReportCreator.createHumidityReport(new File(args(0))), 1.day)

  println("Num of processed files: " + report.filesProcessed)
  println("Num of processed measurements: " + report.measurementsProcessed)
  println("Num of failed measurements: " + report.measurementsFailed)
  println("")
  println("Sensors with highest avg humidity:")
  println("sensor-id,min,avg,max")

  report.sensorStatsSortedByAvg
    .map {
      case (sensorId, Some(stat)) =>
        val avgDisplayed = stat.avg.setScale(2, RoundingMode.HALF_UP).toString()
        s"$sensorId,${stat.min},$avgDisplayed,${stat.max}"
      case (sensorId, None) =>
        s"$sensorId,NaN,NaN,NaN"
    }
    .foreach(println)

  system.terminate()
}
