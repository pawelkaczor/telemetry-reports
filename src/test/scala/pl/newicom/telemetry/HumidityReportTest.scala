package pl.newicom.telemetry

import java.util.UUID

import org.scalacheck.Gen
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class HumidityReportTest extends AnyWordSpecLike with Matchers with ScalaCheckPropertyChecks {

  val statGen: Gen[Option[SensorStats]] =
    Gen.option(for {
      sId <- Gen.alphaStr
      min <- Gen.choose[Int](0, 100)
      max <- Gen.choose[Int](min, 100)
    } yield {
      SensorStats(sId, min, max, BigDecimal(min + max) / 2)
    })

  "sensor statistics" should {
    "be sorted by avg humidity (descending)" in {

      forAll(Gen.listOfN(20, statGen)) { (stats: Seq[Option[SensorStats]]) =>
        val statsMap = stats.map {
          case Some(stat) =>
            (stat.sensorId, Some(stat))
          case None =>
            (UUID.randomUUID().toString, None)
        }.toMap

        val report = HumidityReport(1, 100, 0, statsMap)
        val result = report.sensorStatsSortedByAvg
          .map(_._2)
          .map {
            case Some(stat) => stat.avg
            case None       => BigDecimal(-1)
          }
          .reverse

        result shouldBe sorted
      }
    }
  }
}
