package pl.newicom.telemetry.reporting.zio

import pl.newicom.telemetry.reporting.HumidityReport
import pl.newicom.telemetry.reporting.zio.MeasurementsProvider.MeasurementsProvider
import pl.newicom.telemetry.reporting.zio.Reporting.Service
import pl.newicom.telemetry.{Measurement, MeasurementList, SensorStats}
import zio.stream.ZStream
import zio.test.Assertion.equalTo
import zio.test.{DefaultRunnableSpec, assert, suite, testM}
import zio.{ULayer, ZIO, ZLayer}

object ReportingTest extends DefaultRunnableSpec {
  val measurementsProvider: ULayer[MeasurementsProvider[MeasurementList]] =
    ZLayer.succeed(m => ZStream.fromIterable(m.list))

  val environment = measurementsProvider >>> Reporting.live

  def spec = suite("Reporting") {

    val sources = Seq(
      List(("s1", Some(10)), ("s2", Some(88)), ("s1", None)),
      List(("s2", Some(80)), ("s3", None), ("s2", Some(78)), ("s1", Some(98)))
    ).map(_.map((Measurement.apply _).tupled)).map(MeasurementList)

    testM("test reporting service") {

      for {
        service <- ZIO.service[Service[MeasurementList]].provideLayer(environment)
        report  <- service.humidityReport(sources)
      } yield {
        assert(report)(
          equalTo(
            HumidityReport(
              sourcesProcessed = 2,
              measurementsProcessed = 7,
              measurementsFailed = 2,
              statsBySensor = Map(
                "s1" -> Some(SensorStats("s1", 10, 98, BigDecimal(54))),
                "s2" -> Some(SensorStats("s2", 78, 88, BigDecimal(82))),
                "s3" -> None
              )
            )
          )
        )
      }
    }
  }
}
