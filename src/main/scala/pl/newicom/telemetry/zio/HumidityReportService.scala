package pl.newicom.telemetry.zio

import java.io.File

import pl.newicom.telemetry.{HumidityReport, Measurement, MeasurementIO}
import zio.ZIO.{reduceAll, succeed}
import zio._
import zio.interop.reactivestreams._
import zio.prelude.AssociativeOps
import zio.stream._

class HumidityReportService {
  this: MeasurementIO =>

  def humidityReport(directory: File): ZIO[Any, Throwable, HumidityReport] = {
    val nrOfDailyReports = directory.listFiles().length
    reduceAll(
      succeed(HumidityPartialReport.empty),
      dailyReportStreams(directory)
        .map(_.fold(HumidityPartialReport.empty)(_.withMeasurement(_)))
    )(_ <> _)
      .map(_.finalReport(nrOfDailyReports))
  }

  private def dailyReportStreams(directory: File): Seq[ZStream[Any, Throwable, Measurement]] = {
    directory.listFiles().toSeq.map(f => fromFilePublisher(f).toStream())
  }
}
