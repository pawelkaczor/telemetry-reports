package pl.newicom.telemetry.zio

import java.io.File

import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink}
import pl.newicom.telemetry.DailyReportImport.dailyReportSource
import pl.newicom.telemetry.HumidityReport
import zio.prelude.AssociativeOps

import scala.concurrent.{ExecutionContext, Future}

object ReportCreator {

  def createHumidityReport(directory: File)(implicit m: Materializer, ec: ExecutionContext): Future[HumidityReport] = {
    val nrOfDailyReports = directory.listFiles().length
    Future
      .sequence {
        dailyReportSources(directory)
          .map(_.fold(HumidityPartialReport.empty)(_.withMeasurement(_)))
          .map(_.toMat(Sink.seq)(Keep.right))
          .map(_.run())
      }
      .map(_.flatten)
      .map(_.reduce(_.<>(_)))
      .map(_.buildReport(nrOfDailyReports))
  }

  private def dailyReportSources(directory: File) = {
    directory.listFiles().toSeq.map(dailyReportSource)
  }
}
