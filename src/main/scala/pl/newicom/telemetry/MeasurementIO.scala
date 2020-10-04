package pl.newicom.telemetry

import java.io.File
import java.nio.charset.StandardCharsets

import org.reactivestreams.Publisher

trait MeasurementIO {
  protected val charset = StandardCharsets.UTF_8

  def fromFilePublisher(inputFile: File): Publisher[Measurement]
}
