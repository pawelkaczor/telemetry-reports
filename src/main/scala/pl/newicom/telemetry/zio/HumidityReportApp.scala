package pl.newicom.telemetry.zio

import java.io.File

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import pl.newicom.telemetry.akka.AkkaStreamsMeasurementIO
import zio.{ExitCode, URIO}
import zio.ZIO._
import zio.console.{Console, putStr, putStrLn}

import scala.math.BigDecimal.RoundingMode

object HumidityReportApp extends zio.App {

  implicit val system: ActorSystem = ActorSystem("telemetry", ConfigFactory.load())

  private val reportService = new HumidityReportService with AkkaStreamsMeasurementIO {
    def materializer: Materializer = implicitly[Materializer]
  }

  def run(args: List[String]): URIO[Console, ExitCode] = {
    if (args.isEmpty) {
      throw new IllegalArgumentException("path to csv directory is missing")
    }

    val reportPrintout = for {
      report <- reportService.humidityReport(new File(args.head))
      _      <- putStrLn("Num of processed files: " + report.filesProcessed)
      _      <- putStrLn("Num of processed measurements: " + report.measurementsProcessed)
      _      <- putStrLn("Num of failed measurements: " + report.measurementsFailed)
      _      <- putStrLn("")
      _      <- putStrLn("Sensors with highest avg humidity:")
      _      <- putStrLn("sensor-id,min,avg,max")
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

    (reportPrintout *> fromFuture(_ => system.terminate())).exitCode
  }
}
