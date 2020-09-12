package pl.newicom.telemetry

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

import akka.stream.IOResult
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{FileIO, Source}

import scala.concurrent.Future

object DailyReportImport {

  private val charset = StandardCharsets.UTF_8

  def dailyReportSource(inputFile: File): Source[Measurement, Future[IOResult]] =
    FileIO
      .fromPath(Paths.get(inputFile.toURI))
      .via(lineScanner)
      .via(CsvToMap.toMapAsStrings(charset))
      .map(Measurement.parse)

  private def lineScanner =
    CsvParsing.lineScanner()

}
