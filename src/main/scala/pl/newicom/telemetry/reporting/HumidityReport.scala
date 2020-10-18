package pl.newicom.telemetry.reporting

import org.fusesource.scalate.{TemplateEngine, TemplateSource}
import pl.newicom.telemetry.Measurement.NaN
import pl.newicom.telemetry.SensorStats
import pl.newicom.telemetry.reporting.HumidityReport.fields

import scala.math.BigDecimal.RoundingMode

object HumidityReport {
  val templateEngine = new TemplateEngine()
  val fields         = Seq("sensor-id", "min", "avg", "max")
  val columnWidth    = 12
  val template: String =
    s"""
      |Num of processed files: {{sourcesProcessed}}
      |Num of processed measurements: {{measurementsProcessed}}
      |Num of failed measurements:  {{measurementsFailed}}
      |
      |Sensors with highest avg humidity:
      |-------------------------------------------------------------
      |${fields.map(f => "{{h_" + f + "}}").mkString(" ")}
      |-------------------------------------------------------------
      |{{#sensorStats}}
      |${fields.map(f => "{{" + f + "}}").mkString(" ")}
      |{{/sensorStats}}
      |-------------------------------------------------------------
      |""".stripMargin

  def render(report: HumidityReport): String =
    templateEngine.layout(
      TemplateSource.fromText("pl.newicom.telemetry.report", template).templateType("mustache"),
      Map(
        "sourcesProcessed"      -> report.sourcesProcessed,
        "measurementsProcessed" -> report.measurementsProcessed,
        "measurementsFailed"    -> report.measurementsFailed,
        "sensorStats" -> {
          report.sensorStatsSortedByAvg
            .map(sensorStatsRow)
            .map(r => align(r.toMap))
        }
      ) ++ align(Map("h_sensor-id" -> "Sensor", "h_min" -> "Min", "h_max" -> "Max", "h_avg" -> "Avg"))
    )

  def align(map: Map[String, String]): Map[String, String] =
    map.view.mapValues(_.padTo(columnWidth, " ").mkString("")).toMap

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

case class SensorStatsRow(sensorId: String, min: String, max: String, avg: String) {
  def toMap: Map[String, String] =
    fields.zipAll(Seq(sensorId, min, avg, max), "", "").toMap
}
