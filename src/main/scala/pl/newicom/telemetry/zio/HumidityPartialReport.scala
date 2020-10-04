package pl.newicom.telemetry.zio

import pl.newicom.telemetry.{HumidityReport, Measurement, SensorStats}
import zio.prelude.newtypes.Sum
import zio.prelude.{AssociativeOps, Identity}

object HumidityPartialReport {
  implicit val associative: Identity[HumidityPartialReport] = new Identity[HumidityPartialReport] {
    def identity: HumidityPartialReport = empty

    def combine(l: => HumidityPartialReport, r: => HumidityPartialReport): HumidityPartialReport =
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

  def buildReport(nrOfFiles: Int): HumidityReport = {
    val sensorReports = statsBySensor.map {
      case (sensorId, SensorCumulatedStats.empty) =>
        (sensorId, None)
      case (sensorId, stat) =>
        (sensorId, Some(SensorStats(sensorId, stat.min, stat.max, stat.avg)))
    }

    HumidityReport(nrOfFiles, mProcessed, mFailed, sensorReports)
  }

}
