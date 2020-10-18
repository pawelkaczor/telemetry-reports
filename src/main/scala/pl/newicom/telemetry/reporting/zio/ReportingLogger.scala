package pl.newicom.telemetry.reporting.zio

import _root_.zio.logging.Logging
import pl.newicom.telemetry.MeasurementsSource
import pl.newicom.telemetry.reporting.zio.Reporting.Reporting
import zio.{Tag, ZIO}

object ReportingLogger {
  def log[S <: MeasurementsSource: Tag, R <: Reporting[S] with Logging, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A] =
    ZIO
      .environment[Logging]
      .flatMap { logging =>
        zio.updateService[Reporting.Service[S]] { reporting => (sources: Seq[S]) =>
          reporting
            .humidityReport(sources)
            .tap(_ => logging.get.info("Humidity report created"))
        }
      }
      .asInstanceOf[ZIO[R, E, A]]

  def logging[S <: MeasurementsSource: Tag]: Aspect[Reporting[S] with Logging, Nothing] =
    new Aspect[Reporting[S] with Logging, Nothing] {
      override def apply[R1 <: Reporting[S] with Logging, E1 >: Nothing, A](zio: ZIO[R1, E1, A]): ZIO[R1, E1, A] =
        log[S, R1, E1, A](zio)
    }
}
