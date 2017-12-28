package scalikejdbc

import java.time.ZoneId

trait ZoneIdProvider {
  def value: ZoneId
}

object ZoneIdProvider {
  @inline def apply[A](implicit A: ZoneIdProvider): ZoneIdProvider = A

  def const(zoneId: ZoneId): ZoneIdProvider =
    new ZoneIdProvider {
      val value = zoneId
    }

  def fromFunction(zoneId: () => ZoneId): ZoneIdProvider =
    new ZoneIdProvider {
      def value = zoneId()
    }

  val default: ZoneIdProvider =
    const(ZoneId.systemDefault())

  object Implicits {
    implicit val default: ZoneIdProvider =
      ZoneIdProvider.default
  }
}
