package pl.newicom.telemetry

import org.fusesource.scalate.{TemplateEngine, TemplateSource}
import pl.newicom.telemetry.Measurement.NaN

import scala.math.BigDecimal.RoundingMode

object HumidityReport {
  val templateEngine = new TemplateEngine()
  val template: String =
    """
      |Num of processed files: {{sourcesProcessed}}
      |Num of processed measurements: {{measurementsProcessed}}
      |Num of failed measurements:  {{measurementsFailed}}
      |
      |Sensors with highest avg humidity:
      |sensor-id, min, avg, max
      |{{#sensorStats}}
      |{{sensorId}}, {{min}}, {{avg}}, {{max}}
      |{{/sensorStats}}
      |""".stripMargin

  def render(report: HumidityReport): String =
    templateEngine.layout(
      TemplateSource.fromText("pl.newicom.telemetry.report", template).templateType("mustache"),
      Map(
        "sourcesProcessed"      -> report.sourcesProcessed,
        "measurementsProcessed" -> report.measurementsProcessed,
        "measurementsFailed"    -> report.measurementsFailed,
        "sensorStats" -> report.sensorStatsSortedByAvg
          .map(sensorStatsRow)
          .map(row => Map("sensorId" -> row.sensorId, "min" -> row.min, "avg" -> row.avg, "max" -> row.max))
      )
    )

  private def sensorStatsRow: Function[(String, Option[SensorStats]), SensorStatsRow] = {
    case (sensorId, Some(stat)) =>
      val avgDisplayed = stat.avg.setScale(2, RoundingMode.HALF_UP).toString()
      SensorStatsRow(sensorId, stat.min.toString, stat.max.toString, avgDisplayed)
    case (sensorId, None) => SensorStatsRow(sensorId, NaN, NaN, NaN)
  }
}

case class HumidityReport(
  sourcesProcessed: Int,
  measurementsProcessed: Int,
  measurementsFailed: Int,
  statsBySensor: Map[String, Option[SensorStats]]
) {

  def sensorStatsSortedByAvg: List[(String, Option[SensorStats])] =
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

case class SensorStatsRow(sensorId: String, min: String, max: String, avg: String)
