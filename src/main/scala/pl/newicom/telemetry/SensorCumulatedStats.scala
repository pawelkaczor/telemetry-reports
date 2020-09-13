package pl.newicom.telemetry

object SensorCumulatedStats {
  def apply(hum: Option[Int]): SensorCumulatedStats =
    hum match {
      case Some(value) =>
        NonEmptySensorCumulatedStats(1, value, value, value)
      case None =>
        EmptySensorCumulatedStats
    }
}
sealed trait SensorCumulatedStats {
  def merge(other: SensorCumulatedStats): SensorCumulatedStats
}

case object EmptySensorCumulatedStats extends SensorCumulatedStats {

  def merge(other: SensorCumulatedStats): SensorCumulatedStats =
    other match {
      case EmptySensorCumulatedStats       => this
      case _: NonEmptySensorCumulatedStats => other
    }
}

case class NonEmptySensorCumulatedStats(mSuccessful: Int, cumulated: Long, min: Int, max: Int) extends SensorCumulatedStats {

  def avg: BigDecimal =
    BigDecimal(cumulated) / mSuccessful

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
