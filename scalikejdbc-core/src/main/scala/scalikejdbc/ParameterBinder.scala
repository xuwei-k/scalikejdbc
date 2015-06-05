package scalikejdbc

import java.sql.PreparedStatement

/**
 * ParameterBinder which enables customizing StatementExecutor#binParams.
 *
 * {{{
 * val bytes = Array[Byte](1,2,3, ...)
 * val in = ByteArrayInputStream(bytes)
 * val bin = ParameterBinder(
 *   value = in,
 *   binder = (stmt, idx) => stmt.setBinaryStream(idx, in, bytes.length)
 * )
 * sql"insert into table (bin) values (${bin})".update.apply()
 * }}}
 */
trait ParameterBinder {

  /**
   * Applies parameter to PreparedStatement.
   */
  def apply(stmt: PreparedStatement, idx: Int): Unit

}

/**
 * ParameterBinder factory.
 */
object ParameterBinder {

  /**
   * Factory method for ParameterBinder.
   */
  def apply(binder: (PreparedStatement, Int) => Unit): ParameterBinder = {
    new ParameterBinder {
      override def apply(stmt: PreparedStatement, idx: Int): Unit = binder.apply(stmt, idx)
    }
  }

  val NullParameterBinder = new ParameterBinder {
    def apply(stmt: PreparedStatement, idx: Int): Unit = stmt.setObject(idx, null)
  }

}

