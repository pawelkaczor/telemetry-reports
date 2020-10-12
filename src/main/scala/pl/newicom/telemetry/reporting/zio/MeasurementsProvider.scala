package pl.newicom.telemetry.reporting.zio

import java.nio.charset.StandardCharsets
import java.nio.file.Paths

import akka.NotUsed.notUsed
import akka.actor.ActorSystem
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{FileIO, Sink}
import pl.newicom.telemetry.akka.AkkaModule.AkkaModule
import pl.newicom.telemetry.{CsvFiles, Measurement, MeasurementsSource}
import zio.interop.reactivestreams.publisherToStream
import zio.stream.ZStream
import zio.{Has, URLayer, ZLayer}

object MeasurementsProvider {
  type MeasurementsProvider[S <: MeasurementsSource] = Has[MeasurementsProvider.Service[S]]

  val akkaCvsFiles: URLayer[AkkaModule, MeasurementsProvider[CsvFiles]] = ZLayer.fromService((as: ActorSystem) =>
    new Service[CsvFiles] {
      implicit val system: ActorSystem = as

      def measurementStream(source: CsvFiles): ZStream[Any, Throwable, Measurement] =
        FileIO
          .fromPath(Paths.get(source.rootDir))
          .via(CsvParsing.lineScanner())
          .via(CsvToMap.toMapAsStrings(StandardCharsets.UTF_8))
          .map(Measurement.parse)
          .mapMaterializedValue(_ => notUsed())
          .runWith(Sink.asPublisher(fanout = false))
          .toStream()
    }
  )

  trait Service[S <: MeasurementsSource] {
    def measurementStream(source: S): ZStream[Any, Throwable, Measurement]
  }
}
