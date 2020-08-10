package scalikejdbc

object autoNamedValues {
  def debug[E](entity: E, column: ColumnName[E], excludes: String*): Map[SQLSyntax, ParameterBinder] = ???

  def apply[E](entity: E, column: ColumnName[E], excludes: String*): Map[SQLSyntax, ParameterBinder] = ???
}
