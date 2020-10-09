package pl.newicom.telemetry

import _root_.zio._
import _root_.zio.stream._
import pl.newicom.telemetry.zio.MeasurementsProvider._

object MeasurementTestProvider {
  val test: ULayer[MeasurementsProvider[Nothing]] = ZLayer.succeed { (_: Nothing) =>
    {
      ZStream.empty
    }
  }
}
