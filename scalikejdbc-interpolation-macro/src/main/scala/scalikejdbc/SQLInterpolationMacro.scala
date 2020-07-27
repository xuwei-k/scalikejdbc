package scalikejdbc

import scalikejdbc.interpolation.SQLSyntax
import scala.quoted._

/**
 * Macros for dynamic fields validation
 */
object SQLInterpolationMacro {

  def selectDynamic[E: Type](name: Expr[String])(implicit qctx: QuoteContext): Expr[SQLSyntax] = {
    println("aaa")
    import qctx.tasty._

    val nameOpt: Option[String] = name match {
      case Const(value: String) => Some(value)
      case _ => None
    }

    println(typeOf[E].show)

    /*
    typeOf[E] match {
      case a: Product =>
        a.productElementNames
    }

     */

    /*
    // primary constructor args of type E
    val expectedNames = c.weakTypeOf[E].decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }.map { const =>
      const.paramLists.flatMap { _.map(_.name.encodedName.toString.trim) }
    }.getOrElse(Nil)

    nameOpt.map { _name =>
      if (expectedNames.nonEmpty && !expectedNames.contains(_name)) {
        c.error(c.enclosingPosition, s"${c.weakTypeOf[E]}#${_name} not found. Expected fields are ${expectedNames.mkString("#", ", #", "")}.")
      }
    }

    Apply(Select(c.prefix.tree, TermName("field")), List(name))
*/
    ???
  }

}

