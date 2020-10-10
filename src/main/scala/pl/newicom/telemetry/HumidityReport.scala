package pl.newicom.telemetry

case class HumidityReport(
  sourcesProcessed: Int,
  measurementsProcessed: Int,
  measurementsFailed: Int,
  statsBySensor: Map[String, Option[SensorStats]]
) {

  def sensorStatsSortedByAvg: Seq[(String, Option[SensorStats])] =
    statsBySensor.toList.sortWith((s1, s2) => {
      if (s1._2.isEmpty) {
        false
      } else if (s2._2.isEmpty) {
        true
      } else {
        s1._2.get.avg.compareTo(s2._2.get.avg) > 0
      }
    })

}
