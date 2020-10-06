package pl.newicom.telemetry.zio

import java.io.File

import pl.newicom.telemetry.zio.MeasurementIO.MeasurementIO
import pl.newicom.telemetry.{HumidityPartialReport, HumidityReport, Measurement}
import zio.ZIO.{reduceAll, succeed}
import zio._
import zio.interop.reactivestreams._
import zio.prelude.AssociativeOps
import zio.stream._

object Reporting {
  type Reporting = Has[Reporting.Service]

  val live: URLayer[MeasurementIO, Reporting] = ZLayer.fromService(mIo =>
    new Service {

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
        directory.listFiles().toSeq.map(f => mIo.fromFilePublisher(f).toStream())
      }
    }
  )

  trait Service {
    def humidityReport(directory: File): ZIO[Any, Throwable, HumidityReport]
  }
}
