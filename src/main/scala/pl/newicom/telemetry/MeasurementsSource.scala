package pl.newicom.telemetry

import java.net.URI

trait MeasurementsSource

case class CsvFiles(rootDir: URI)                   extends MeasurementsSource
case class MeasurementList(list: List[Measurement]) extends MeasurementsSource
