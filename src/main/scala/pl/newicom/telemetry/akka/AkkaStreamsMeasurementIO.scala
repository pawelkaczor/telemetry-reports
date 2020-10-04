package pl.newicom.telemetry.akka

import java.io.File
import java.nio.file.Paths

import akka.NotUsed.notUsed
import akka.stream.Materializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{FileIO, Sink}
import org.reactivestreams.Publisher
import pl.newicom.telemetry.{Measurement, MeasurementIO}

trait AkkaStreamsMeasurementIO extends MeasurementIO {

  implicit def materializer: Materializer

  def fromFilePublisher(inputFile: File): Publisher[Measurement] =
    FileIO
      .fromPath(Paths.get(inputFile.toURI))
      .via(lineScanner)
      .via(CsvToMap.toMapAsStrings(charset))
      .map(Measurement.parse)
      .mapMaterializedValue(_ => notUsed())
      .runWith(Sink.asPublisher(fanout = false))

  private def lineScanner =
    CsvParsing.lineScanner()

}
