package pl.newicom.telemetry.zio

import pl.newicom.telemetry.zio.MeasurementsProvider.MeasurementsProvider
import pl.newicom.telemetry.{HumidityPartialReport, HumidityReport, MeasurementsSource}
import zio.ZIO.{reduceAll, succeed}
import zio._
import zio.prelude.AssociativeOps

object Reporting {
  type Reporting = Has[Reporting.Service]

  val live: URLayer[MeasurementsProvider[MeasurementsSource], Reporting] = ZLayer.fromService(mp =>
    new Service {
      def humidityReport(sources: Seq[MeasurementsSource]): ZIO[Any, Throwable, HumidityReport] = {
        val nrOfDailyReports = sources.size
        reduceAll(
          succeed(HumidityPartialReport.empty),
          sources
            .map(s => mp.measurementStream(s))
            .map(_.fold(HumidityPartialReport.empty)(_.withMeasurement(_)))
        )(_ combine _)
          .map(_.finalReport(nrOfDailyReports))
      }
    }
  )

  trait Service {
    def humidityReport(sources: Seq[MeasurementsSource]): ZIO[Any, Throwable, HumidityReport]
  }
}
