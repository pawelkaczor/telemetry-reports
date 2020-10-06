package pl.newicom.telemetry.zio

import java.io.File

import pl.newicom.telemetry.akka.AkkaIO
import pl.newicom.telemetry.zio.Reporting.Reporting
import zio.ZIO._
import zio.console.{Console, putStr, putStrLn}
import zio.{ExitCode, URIO, ZIO}

import scala.math.BigDecimal.RoundingMode

object HumidityReportApp extends zio.App {

  def run(args: List[String]): URIO[Console, ExitCode] = {
    if (args.isEmpty) {
      throw new IllegalArgumentException("path to csv directory is missing")
    }

    val environment =
      Console.live ++ (AkkaIO.live >>> MeasurementIO.akkaLive >>> Reporting.live)

    val program = for {
      reporting <- ZIO.environment[Reporting]
      report    <- reporting.get.humidityReport(new File(args.head))
      _         <- putStrLn("Num of processed files: " + report.filesProcessed)
      _         <- putStrLn("Num of processed measurements: " + report.measurementsProcessed)
      _         <- putStrLn("Num of failed measurements: " + report.measurementsFailed)
      _         <- putStrLn("")
      _         <- putStrLn("Sensors with highest avg humidity:")
      _         <- putStrLn("sensor-id,min,avg,max")
      _ <- reduceAll(
        putStr(""),
        report.sensorStatsSortedByAvg.map {
          case (sensorId, Some(stat)) =>
            val avgDisplayed = stat.avg.setScale(2, RoundingMode.HALF_UP).toString()
            putStrLn(s"$sensorId,${stat.min},$avgDisplayed,${stat.max}")
          case (sensorId, None) =>
            putStrLn(s"$sensorId,NaN,NaN,NaN")
        }
      )((_, _) => ())
    } yield {}

    program.provideLayer(environment).exitCode
  }
}
