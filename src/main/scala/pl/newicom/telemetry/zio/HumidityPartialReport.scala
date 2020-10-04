package pl.newicom.telemetry.zio

import pl.newicom.telemetry.{HumidityReport, Measurement, SensorStats}
import zio.prelude.newtypes.Sum
import zio.prelude.{AssociativeOps, Identity}

object HumidityPartialReport {
  implicit val associative: Identity[HumidityPartialReport] = new Identity[HumidityPartialReport] {
    def identity: HumidityPartialReport = HumidityPartialReport(Sum(0), Sum(0), Sum(0), Map.empty)

    def combine(l: => HumidityPartialReport, r: => HumidityPartialReport): HumidityPartialReport = HumidityPartialReport(
      l.fProcessed <> r.fProcessed,
      l.mProcessed <> r.mProcessed,
      l.mFailed <> r.mFailed,
      l.statsBySensor <> r.statsBySensor
    )
  }

  def initial(nrOfFiles: Int): HumidityPartialReport = associative.identity.copy(fProcessed = Sum(nrOfFiles))

}
case class HumidityPartialReport(
  fProcessed: Sum[Int],
  mProcessed: Sum[Int],
  mFailed: Sum[Int],
  statsBySensor: Map[String, SensorCumulatedStats]
) {

  def withMeasurement(m: Measurement): HumidityPartialReport = {
    val newStat = statsBySensor.getOrElse(m.sensorId, SensorCumulatedStats.empty) <> SensorCumulatedStats(m.humidity)

    val newStatsBySensor = statsBySensor + (m.sensorId -> newStat)
    val newNrOfFailed    = mFailed <> Sum(if (m.isFailed) 1 else 0)

    copy(mProcessed = mProcessed <> Sum(1), mFailed = newNrOfFailed, statsBySensor = newStatsBySensor)
  }

  def buildReport: HumidityReport = {
    val sensorReports = statsBySensor.map {
      case (sensorId, SensorCumulatedStats.empty) =>
        (sensorId, None)
      case (sensorId, stat) =>
        (sensorId, Some(SensorStats(sensorId, stat.min, stat.max, stat.avg)))
    }

    HumidityReport(fProcessed, mProcessed, mFailed, sensorReports)
  }

}
