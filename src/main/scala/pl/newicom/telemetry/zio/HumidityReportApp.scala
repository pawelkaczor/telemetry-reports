package pl.newicom.telemetry.zio

import java.io.File

import pl.newicom.telemetry.akka.AkkaModule
import pl.newicom.telemetry.zio.Reporting.Reporting
import pl.newicom.telemetry.{CvsFiles, HumidityReport}
import zio.console.{Console, putStrLn}
import zio.{ExitCode, URIO, ZIO}

object HumidityReportApp extends zio.App {

  def run(args: List[String]): URIO[Console, ExitCode] = {
    if (args.isEmpty) {
      throw new IllegalArgumentException("path to csv directory is missing")
    }

    val environment =
      Console.live ++ (AkkaModule.live >>> MeasurementsProvider.akkaCvsFiles >>> Reporting.live)

    def sources: Seq[CvsFiles] = new File(args.head).listFiles().toSeq.map(f => CvsFiles(f.toURI))

    val program = for {
      reporting <- ZIO.environment[Reporting[CvsFiles]]
      report    <- reporting.get.humidityReport(sources)
      _         <- putStrLn(HumidityReport.render(report))
    } yield {}

    program.provideLayer(environment).exitCode
  }
}
