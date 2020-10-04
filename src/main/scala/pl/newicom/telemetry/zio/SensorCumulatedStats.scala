package pl.newicom.telemetry.zio

import zio.prelude.newtypes.{Max, Min, Sum}
import zio.prelude.{AssociativeOps, Identity}

object SensorCumulatedStats {
  val empty: SensorCumulatedStats = SensorCumulatedStats(Sum(0), Sum(0), Min(Int.MinValue), Max(Int.MaxValue))

  def apply(hum: Option[Int]): SensorCumulatedStats =
    hum match {
      case Some(value) =>
        SensorCumulatedStats(Sum(1), Sum(value), Min(value), Max(value))
      case None =>
        associative.identity
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
