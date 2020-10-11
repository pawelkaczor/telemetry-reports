package pl.newicom.telemetry.reporting.zio

import pl.newicom.telemetry.MeasurementsSource
import pl.newicom.telemetry.reporting.zio.MeasurementsProvider.MeasurementsProvider
import pl.newicom.telemetry.reporting.{HumidityPartialReport, HumidityReport}
import zio.ZIO.{reduceAll, succeed}
import zio._
import zio.prelude.AssociativeOps

object Reporting {
  type Reporting[S <: MeasurementsSource] = Has[Reporting.Service[S]]

  def live[S <: MeasurementsSource: Tag]: URLayer[MeasurementsProvider[S], Reporting[S]] =
    ZLayer.fromService(provider =>
      (sources: Seq[S]) => {
        reduceAll(
          succeed(HumidityPartialReport.empty),
          sources
            .map(provider.measurementStream)
            .map(_.fold(HumidityPartialReport.empty)(_.withMeasurement(_)))
        )(_ combine _).map(_.finalReport(sources.size))
      }
    )

  trait Service[S <: MeasurementsSource] {
    def humidityReport(sources: Seq[S]): ZIO[Any, Throwable, HumidityReport]
  }
}
