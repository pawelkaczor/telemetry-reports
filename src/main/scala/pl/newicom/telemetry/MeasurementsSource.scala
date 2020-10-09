package pl.newicom.telemetry

import java.net.URI

trait MeasurementsSource

case class CvsFiles(rootDir: URI) extends MeasurementsSource
