package scalikejdbc

import scala.language.implicitConversions
import java.sql.{ Date => sqlDate, Time => sqlTime, Timestamp => sqlTimestamp }
import java.util.{ Date => utilDate }

/**
 * Implicit conversions for date time values.
 */
trait UnixTimeInMillisConverterImplicits {

  implicit def convertJavaUtilDateToConverter(t: utilDate): UnixTimeInMillisConverter = new UnixTimeInMillisConverter(t)

}

object UnixTimeInMillisConverterImplicits extends UnixTimeInMillisConverterImplicits
