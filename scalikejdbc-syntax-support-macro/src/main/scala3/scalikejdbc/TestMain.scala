package foo

import scalikejdbc._

case class Foo(a: Int, b: String)

object TestMain {

  def a: SQLSyntaxSupport[Foo] = SQLSyntaxSupportFactory.apply[Foo]()

  def main(args: Array[String]): Unit = {
    val x = scalikejdbc.autoNamedValues(Foo(2, "a"), a.column)
    println(x)
  }

}
