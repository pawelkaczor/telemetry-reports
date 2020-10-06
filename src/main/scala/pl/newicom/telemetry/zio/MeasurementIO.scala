package pl.newicom.telemetry.zio

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

import akka.NotUsed.notUsed
import akka.actor.ActorSystem
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{FileIO, Sink}
import org.reactivestreams.Publisher
import pl.newicom.telemetry.Measurement
import pl.newicom.telemetry.akka.AkkaIO.AkkaIO
import zio._

object MeasurementIO {
  type MeasurementIO = Has[MeasurementIO.Service]

  val akkaLive: URLayer[AkkaIO, MeasurementIO] = ZLayer.fromService((as: ActorSystem) =>
    new Service {
      implicit val system: ActorSystem = as
      def fromFilePublisher(inputFile: File): Publisher[Measurement] =
        FileIO
          .fromPath(Paths.get(inputFile.toURI))
          .via(CsvParsing.lineScanner())
          .via(CsvToMap.toMapAsStrings(charset))
          .map(Measurement.parse)
          .mapMaterializedValue(_ => notUsed())
          .runWith(Sink.asPublisher(fanout = false))
    }
  )

  trait Service {
    protected val charset = StandardCharsets.UTF_8
    def fromFilePublisher(inputFile: File): Publisher[Measurement]
  }
}
