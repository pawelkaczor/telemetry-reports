package pl.newicom.telemetry

import scala.collection.decorators._

case class HumidityReport(
  filesProcessed: Int,
  measurementsProcessed: Int,
  measurementsFailed: Int,
  sensors: Map[String, Option[SensorStats]]
) {

  def sensorStatsSortedByAvg: Seq[(String, Option[SensorStats])] =
    sensors.toList.sortWith((s1, s2) => {
      if (s1._2.isEmpty) {
        false
      } else if (s2._2.isEmpty) {
        true
      } else {
        s1._2.get.avg.compareTo(s2._2.get.avg) < 0
      }
    })

}

object ReportBuilder {
  def initial(nrOfFiles: Int): ReportBuilder = ReportBuilder(nrOfFiles, 0, 0, Map.empty)
}

case class ReportBuilder(fProcessed: Int, mProcessed: Int, mFailed: Int, sensors: Map[String, SensorCumulatedStats]) {

  def merge(other: ReportBuilder): ReportBuilder =
    copy(
      fProcessed + other.fProcessed,
      mProcessed + other.mProcessed,
      mFailed + other.mFailed,
      sensors.mergeByKeyWith(other.sensors) {
        case (Some(s1), None)     => s1
        case (None, Some(s2))     => s2
        case (Some(s1), Some(s2)) => s1.merge(s2)
      }
    )

  def withMeasurement(m: Measurement): ReportBuilder = {
    val newStat = m.humidity
      .map(hum => {
        sensors
          .getOrElse[SensorCumulatedStats](m.sensorId, EmptySensorCumulatedStats)
          .withSuccessfulMeasurement(hum)
      })
      .getOrElse(EmptySensorCumulatedStats)

    val newSensors    = sensors + (m.sensorId -> newStat)
    val newNrOfFailed = mFailed + (if (m.isFailed) 1 else 0)

    copy(mProcessed = mProcessed + 1, mFailed = newNrOfFailed, sensors = newSensors)
  }

  def buildReport: HumidityReport = {
    val sensorReports = sensors.map {
      case (sensorId, EmptySensorCumulatedStats) =>
        (sensorId, None)
      case (sensorId, stat: NonEmptySensorCumulatedStats) =>
        (sensorId, Some(SensorStats(sensorId, stat.min, stat.max, stat.avg)))
    }

    HumidityReport(fProcessed, mProcessed, mFailed, sensorReports)
  }

}
