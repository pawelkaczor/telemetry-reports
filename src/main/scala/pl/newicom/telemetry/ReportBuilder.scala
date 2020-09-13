package pl.newicom.telemetry
import scala.collection.decorators._

case class ReportBuilder(fProcessed: Int, mProcessed: Int, mFailed: Int, statsBySensor: Map[String, SensorCumulatedStats]) {

  def merge(other: ReportBuilder): ReportBuilder =
    copy(
      fProcessed + other.fProcessed,
      mProcessed + other.mProcessed,
      mFailed + other.mFailed,
      statsBySensor.mergeByKeyWith(other.statsBySensor) {
        case (Some(s1), None)     => s1
        case (None, Some(s2))     => s2
        case (Some(s1), Some(s2)) => s1.merge(s2)
      }
    )

  def withMeasurement(m: Measurement): ReportBuilder = {
    val newStat = statsBySensor
      .getOrElse(m.sensorId, EmptySensorCumulatedStats)
      .merge(SensorCumulatedStats(m.humidity))

    val newStatsBySensor = statsBySensor + (m.sensorId -> newStat)
    val newNrOfFailed    = mFailed + (if (m.isFailed) 1 else 0)

    copy(mProcessed = mProcessed + 1, mFailed = newNrOfFailed, statsBySensor = newStatsBySensor)
  }

  def buildReport: HumidityReport = {
    val sensorReports = statsBySensor.map {
      case (sensorId, EmptySensorCumulatedStats) =>
        (sensorId, None)
      case (sensorId, stat: NonEmptySensorCumulatedStats) =>
        (sensorId, Some(SensorStats(sensorId, stat.min, stat.max, stat.avg)))
    }

    HumidityReport(fProcessed, mProcessed, mFailed, sensorReports)
  }

}

object ReportBuilder {
  def initial(nrOfFiles: Int): ReportBuilder = ReportBuilder(nrOfFiles, 0, 0, Map.empty)
}
