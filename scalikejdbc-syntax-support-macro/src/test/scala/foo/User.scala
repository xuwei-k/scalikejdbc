package foo

import scalikejdbc.SQLSyntaxSupportImpl
case class User(id: Long, name: String)

object User {
  val table: SQLSyntaxSupportImpl[User] = scalikejdbc.SQLSyntaxSupportFactory[User]()
}
