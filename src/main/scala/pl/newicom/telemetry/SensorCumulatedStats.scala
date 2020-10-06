package pl.newicom.telemetry

import _root_.zio.prelude.Identity
import _root_.zio.prelude.newtypes._

object SensorCumulatedStats {
  val empty: SensorCumulatedStats = SensorCumulatedStats(Sum(0), Sum(0), Min(Int.MaxValue), Max(Int.MinValue))

  def apply(hum: Option[Int]): SensorCumulatedStats =
    hum match {
      case Some(value) =>
        SensorCumulatedStats(Sum(1), Sum(value), Min(value), Max(value))
      case None =>
        empty
    }

  implicit val associative: Identity[SensorCumulatedStats] = new Identity[SensorCumulatedStats] {
    def identity: SensorCumulatedStats = empty

    def combine(l: => SensorCumulatedStats, r: => SensorCumulatedStats): SensorCumulatedStats =
      SensorCumulatedStats(l.mSuccessful <> r.mSuccessful, l.cumulated <> r.cumulated, l.min <> r.min, l.max <> r.max)
  }

}

case class SensorCumulatedStats(mSuccessful: Sum[Int], cumulated: Sum[Long], min: Min[Int], max: Max[Int]) {
  def avg: BigDecimal =
    BigDecimal(cumulated) / mSuccessful
}
