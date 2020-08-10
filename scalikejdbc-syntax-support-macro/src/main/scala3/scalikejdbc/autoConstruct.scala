package scalikejdbc

object autoConstruct {
  def apply[A](rs: WrappedResultSet, rn: ResultName[A], excludes: String*): A = ???

  def apply[A](rs: WrappedResultSet, sp: SyntaxProvider[A], excludes: String*): A = ???

  def debug[A](rs: WrappedResultSet, rn: ResultName[A], excludes: String*): A = ???

  def debug[A](rs: WrappedResultSet, sp: SyntaxProvider[A], excludes: String*): A = ???
}
