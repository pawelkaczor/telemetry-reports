package pl.newicom.telemetry.reporting

import _root_.zio.prelude._
import _root_.zio.prelude.newtypes._
import pl.newicom.telemetry.{Measurement, SensorCumulatedStats, SensorStats}

object HumidityPartialReport {
  implicit val associative: Associative[HumidityPartialReport] = Associative.make { (l, r) =>
    HumidityPartialReport(l.mProcessed <> r.mProcessed, l.mFailed <> r.mFailed, l.statsBySensor <> r.statsBySensor)
  }

  def empty: HumidityPartialReport = HumidityPartialReport(Sum(0), Sum(0), Map.empty)

}
case class HumidityPartialReport(
  mProcessed: Sum[Int], // number of processed measurements
  mFailed: Sum[Int],    // number of failed measurements
  statsBySensor: Map[String, SensorCumulatedStats]
) {

  def withMeasurement(m: Measurement): HumidityPartialReport = {
    val newStat = statsBySensor.getOrElse(m.sensorId, SensorCumulatedStats.empty) <> SensorCumulatedStats(m.humidity)

    val newStatsBySensor = statsBySensor + (m.sensorId -> newStat)
    val newNrOfFailed    = mFailed <> Sum(if (m.isFailed) 1 else 0)

    copy(mProcessed = mProcessed <> Sum(1), mFailed = newNrOfFailed, statsBySensor = newStatsBySensor)
  }

  def finalReport(nrOfFiles: Int): HumidityReport = {
    val sensorReports = statsBySensor.map {
      case (sensorId, SensorCumulatedStats.empty) =>
        (sensorId, None)
      case (sensorId, stat) =>
        (sensorId, Some(SensorStats(sensorId, stat.min, stat.max, stat.avg)))
    }

    HumidityReport(nrOfFiles, mProcessed, mFailed, sensorReports)
  }

}
