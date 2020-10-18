package pl.newicom.telemetry.reporting.zio.app

import java.io.{File, FilenameFilter}

import pl.newicom.telemetry.CsvFiles
import pl.newicom.telemetry.akka.AkkaModule
import pl.newicom.telemetry.reporting.HumidityReport
import pl.newicom.telemetry.reporting.zio.Aspect.AspectSyntax
import pl.newicom.telemetry.reporting.zio.Reporting.Reporting
import pl.newicom.telemetry.reporting.zio.ReportingLogger.logging
import pl.newicom.telemetry.reporting.zio.{MeasurementsProvider, Reporting}
import zio.console.putStrLn
import zio.logging._
import zio.{ExitCode, URIO, ZEnv, ZIO}

object HumidityReportApp extends zio.App {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    if (args.isEmpty) {
      throw new IllegalArgumentException("path to csv directory is missing")
    }

    val environment =
      Logging.console() ++ (AkkaModule.live >>> MeasurementsProvider.akkaCvsFiles >>> Reporting.live[CsvFiles])

    def sources: Seq[CsvFiles] = new File(args.head)
      .listFiles(new FilenameFilter {
        def accept(dir: File, name: String): Boolean = name.endsWith("csv")
      })
      .toSeq
      .map(f => CsvFiles(f.toURI))

    val program = for {
      reporting <- ZIO.environment[Reporting[CsvFiles]] @@ logging[CsvFiles]
      report    <- reporting.get.humidityReport(sources)
      _         <- putStrLn(HumidityReport.render(report))
    } yield {}

    program.provideCustomLayer(environment).exitCode
  }
}
