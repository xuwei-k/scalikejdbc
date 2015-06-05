package scalikejdbc

import java.io.InputStream
import java.sql.PreparedStatement
import scalikejdbc.UnixTimeInMillisConverterImplicits._
import scalikejdbc.interpolation.SQLSyntax
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

private[scalikejdbc] case class SQLSyntaxParameterBinder(syntax: SQLSyntax) extends ParameterBinder {
  def apply(stmt: PreparedStatement, idx: Int): Unit = ()
}

trait ParameterBinderFactory[A] { self =>

  def apply(value: A): ParameterBinder

  def contramap[B](f: B => A): ParameterBinderFactory[B] = new ParameterBinderFactory[B] {
    def apply(value: B): ParameterBinder = {
      if (value == null) ParameterBinder.NullParameterBinder
      else self(f(value))
    }
  }

}

object ParameterBinderFactory extends LowPriorityImplicitsParameterBinderFactory1 {

  def apply[A](f: A => (PreparedStatement, Int) => Unit): ParameterBinderFactory[A] = new ParameterBinderFactory[A] {
    def apply(value: A): ParameterBinder = {
      if (value == null) ParameterBinder.NullParameterBinder
      else ParameterBinder(f(value))
    }
  }

  implicit val intTypeUnbinder: ParameterBinderFactory[Int] = ParameterBinderFactory { v => (ps, idx) => ps.setInt(idx, v) }
  implicit val stringTypeUnbinder: ParameterBinderFactory[String] = ParameterBinderFactory { v => (ps, idx) => ps.setString(idx, v) }
  implicit val sqlArrayTypeUnbinder: ParameterBinderFactory[java.sql.Array] = ParameterBinderFactory { v => (ps, idx) => ps.setArray(idx, v) }
  implicit val bigDecimalTypeUnbinder: ParameterBinderFactory[BigDecimal] = ParameterBinderFactory { v => (ps, idx) => ps.setBigDecimal(idx, v.bigDecimal) }
  implicit val booleanTypeUnbinder: ParameterBinderFactory[Boolean] = ParameterBinderFactory { v => (ps, idx) => ps.setBoolean(idx, v) }
  implicit val byteTypeUnbinder: ParameterBinderFactory[Byte] = ParameterBinderFactory { v => (ps, idx) => ps.setByte(idx, v) }
  implicit val sqlDateTypeUnbinder: ParameterBinderFactory[java.sql.Date] = ParameterBinderFactory { v => (ps, idx) => ps.setDate(idx, v) }
  implicit val doubleTypeUnbinder: ParameterBinderFactory[Double] = ParameterBinderFactory { v => (ps, idx) => ps.setDouble(idx, v) }
  implicit val floatTypeUnbinder: ParameterBinderFactory[Float] = ParameterBinderFactory { v => (ps, idx) => ps.setFloat(idx, v) }
  implicit val longTypeUnbinder: ParameterBinderFactory[Long] = ParameterBinderFactory { v => (ps, idx) => ps.setLong(idx, v) }
  implicit val shortTypeUnbinder: ParameterBinderFactory[Short] = ParameterBinderFactory { v => (ps, idx) => ps.setShort(idx, v) }
  implicit val sqlXmlTypeUnbinder: ParameterBinderFactory[java.sql.SQLXML] = ParameterBinderFactory { v => (ps, idx) => ps.setSQLXML(idx, v) }
  implicit val sqlTimeTypeUnbinder: ParameterBinderFactory[java.sql.Time] = ParameterBinderFactory { v => (ps, idx) => ps.setTime(idx, v) }
  implicit val sqlTimestampTypeUnbinder: ParameterBinderFactory[java.sql.Timestamp] = ParameterBinderFactory { v => (ps, idx) => ps.setTimestamp(idx, v) }
  implicit val urlTypeUnbinder: ParameterBinderFactory[java.net.URL] = ParameterBinderFactory { v => (ps, idx) => ps.setURL(idx, v) }
  implicit val utilDateTypeUnbinder: ParameterBinderFactory[java.util.Date] = sqlTimestampTypeUnbinder.contramap(_.toSqlTimestamp)
  implicit val jodaDateTimeTypeUnbinder: ParameterBinderFactory[org.joda.time.DateTime] = utilDateTypeUnbinder.contramap(_.toDate)
  implicit val jodaLocalDateTimeTypeUnbinder: ParameterBinderFactory[org.joda.time.LocalDateTime] = utilDateTypeUnbinder.contramap(_.toDate)
  implicit val jodaLocalDateTypeUnbinder: ParameterBinderFactory[org.joda.time.LocalDate] = sqlDateTypeUnbinder.contramap(_.toDate.toSqlDate)
  implicit val jodaLocalTimeTypeUnbinder: ParameterBinderFactory[org.joda.time.LocalTime] = sqlTimeTypeUnbinder.contramap(_.toSqlTime)
  implicit val inputStreamTypeUnbinder: ParameterBinderFactory[InputStream] = ParameterBinderFactory { v => (ps, idx) => ps.setBinaryStream(idx, v) }
  implicit val nullTypeUnbinder: ParameterBinderFactory[Null] = new ParameterBinderFactory[Null] { def apply(value: Null) = ParameterBinder.NullParameterBinder }
  implicit val noneTypeUnbinder: ParameterBinderFactory[None.type] = new ParameterBinderFactory[None.type] { def apply(value: None.type) = ParameterBinder.NullParameterBinder }
  implicit val sqlSyntaxUnbinder: ParameterBinderFactory[SQLSyntax] = new ParameterBinderFactory[SQLSyntax] { def apply(value: SQLSyntax) = SQLSyntaxParameterBinder(value) }
  implicit val optionalSqlSyntaxUnbinder: ParameterBinderFactory[Option[SQLSyntax]] = sqlSyntaxUnbinder.contramap(_ getOrElse SQLSyntax.empty)

}

trait LowPriorityImplicitsParameterBinderFactory1 extends LowPriorityImplicitsParameterBinderFactory0 {

  implicit def optionalTypeUnbinder[A](implicit ev: ParameterBinderFactory[A]): ParameterBinderFactory[Option[A]] = new ParameterBinderFactory[Option[A]] {
    def apply(value: Option[A]): ParameterBinder = {
      if (value == null) ParameterBinder.NullParameterBinder
      else value.fold(ParameterBinder.NullParameterBinder)(ev.apply)
    }
  }

  def jsr310TypeUnbinder[A]: ParameterBinderFactory[A] = ParameterBinderFactory[A] { p => (underlying, i) =>
    // Accessing JSR-310 APIs via Java reflection
    // because scalikejdbc-core should work on not only Java 8 but 6 & 7.
    import java.lang.reflect.Method
    val className: String = p.getClass.getCanonicalName
    val clazz: Class[_] = Class.forName(className)
    className match {
      case "java.time.ZonedDateTime" | "java.time.OffsetDateTime" =>
        val instant = clazz.getMethod("toInstant").invoke(p) // java.time.Instant
        val dateClazz: Class[_] = Class.forName("java.util.Date") // java.util.Date
        val fromMethod: Method = dateClazz.getMethod("from", Class.forName("java.time.Instant"))
        val dateValue = fromMethod.invoke(null, instant).asInstanceOf[java.util.Date]
        underlying.setTimestamp(i, dateValue.toSqlTimestamp)
      case "java.time.LocalDateTime" =>
        underlying.setTimestamp(i, org.joda.time.LocalDateTime.parse(p.toString).toDate.toSqlTimestamp)
      case "java.time.LocalDate" =>
        underlying.setDate(i, org.joda.time.LocalDate.parse(p.toString).toDate.toSqlDate)
      case "java.time.LocalTime" =>
        underlying.setTime(i, org.joda.time.LocalTime.parse(p.toString).toSqlTime)
    }
  }

}

trait LowPriorityImplicitsParameterBinderFactory0 {
  def anyTypeUnbinder[A]: ParameterBinderFactory[A] = macro ParameterBinderFactoryMacro.any[A]
}

private[scalikejdbc] object ParameterBinderFactoryMacro {

  def any[A: c.WeakTypeTag](c: Context): c.Expr[ParameterBinderFactory[A]] = {
    import c.universe._
    val A = weakTypeTag[A].tpe
    if (A.toString.startsWith("java.time.")) c.Expr[ParameterBinderFactory[A]](q"scalikejdbc.TypeUnbinder.jsr310TypeUnbinder[$A]")
    else c.abort(c.enclosingPosition, s"Could not find an implicit value of the TypeUnbinder[$A].")
  }

}
