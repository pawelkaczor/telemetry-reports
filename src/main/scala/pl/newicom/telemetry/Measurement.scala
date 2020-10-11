package pl.newicom.telemetry

object Measurement {
  val SensorId: String = "sensor-id"
  val Humidity: String = "humidity"
  val NaN: String      = "NaN"

  def parse(csvRecord: Map[String, String]): Measurement =
    Measurement(
      csvRecord(SensorId),
      Some(csvRecord(Humidity)).flatMap {
        case NaN   => None
        case value => Some(Integer.valueOf(value))
      }
    )
}

case class Measurement(sensorId: String, humidity: Option[Int]) {
  def isFailed: Boolean = humidity.isEmpty
}
