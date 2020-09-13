package pl.newicom.telemetry

import java.io.File

import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink}
import pl.newicom.telemetry.DailyReportImport.dailyReportSource

import scala.concurrent.{ExecutionContext, Future}

object ReportCreator {

  def createHumidityReport(directory: File)(implicit m: Materializer, ec: ExecutionContext): Future[HumidityReport] = {
    val nrOfDailyReports = directory.listFiles().length
    val repBuilder       = ReportBuilder.initial(nrOfDailyReports)

    Future
      .sequence {
        dailyReportSources(directory)
          .map(_.scan(repBuilder)(_.withMeasurement(_)))
          .map(_.toMat(Sink.seq)(Keep.right))
          .map(_.run())
      }
      .map(_.flatten)
      .map(_.reduce(_.merge(_)))
      .map(_.buildReport)
  }

  private def dailyReportSources(directory: File) = {
    directory.listFiles().toSeq.map(dailyReportSource)
  }
}
