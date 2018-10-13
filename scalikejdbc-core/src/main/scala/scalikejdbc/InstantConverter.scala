package scalikejdbc

import java.util.Calendar

class InstantConverter(override val toInstant: java.time.Instant) extends AnyVal with TimeConverter {
  override protected[this] def millis: Long =
    toInstant.toEpochMilli

  override def toSqlTimestamp: java.sql.Timestamp =
    java.sql.Timestamp.from(toInstant)
}
