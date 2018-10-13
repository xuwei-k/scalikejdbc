package scalikejdbc

import java.util.Calendar

/**
 * Unix Time Converter to several types.
 *
 * @param millis the milliseconds from 1970-01-01T00:00:00Z
 */
class UnixTimeInMillisConverter(protected override val millis: Long) extends AnyVal with TimeConverter {
  override def toInstant: java.time.Instant =
    java.time.Instant.ofEpochMilli(millis)

  override def toSqlTimestamp: java.sql.Timestamp =
    new java.sql.Timestamp(millis)
}
