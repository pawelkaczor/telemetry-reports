package pl.newicom.telemetry

sealed trait SensorCumulatedStats {
  def merge(other: SensorCumulatedStats): SensorCumulatedStats
  def withSuccessfulMeasurement(humidity: Int): SensorCumulatedStats
}

case object EmptySensorCumulatedStats extends SensorCumulatedStats {
  def withSuccessfulMeasurement(hum: Int): SensorCumulatedStats =
    NonEmptySensorCumulatedStats(1, cumulated = hum, min = hum, max = hum)

  def merge(other: SensorCumulatedStats): SensorCumulatedStats =
    other match {
      case EmptySensorCumulatedStats       => this
      case _: NonEmptySensorCumulatedStats => other
    }
}

case class NonEmptySensorCumulatedStats(mSuccessful: Int, cumulated: Long, min: Int, max: Int) extends SensorCumulatedStats {

  def avg: BigDecimal =
    BigDecimal(cumulated) / mSuccessful

  def withSuccessfulMeasurement(hum: Int): NonEmptySensorCumulatedStats =
    NonEmptySensorCumulatedStats(mSuccessful + 1, cumulated + hum, Math.min(min, hum), Math.max(max, hum))

  def merge(other: SensorCumulatedStats): SensorCumulatedStats =
    other match {
      case EmptySensorCumulatedStats =>
        this
      case o: NonEmptySensorCumulatedStats =>
        NonEmptySensorCumulatedStats(
          mSuccessful + o.mSuccessful,
          cumulated + o.cumulated,
          Math.min(min, o.min),
          Math.max(max, o.max)
        )
    }
}
