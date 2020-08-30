package scalikejdbc

import scalikejdbc.interpolation.SQLSyntax
import scala.quoted._
import scala.compiletime.error
import scalikejdbc.SQLSyntaxSupportFeature

/**
 * Macros for dynamic fields validation
 */
object SQLInterpolationMacro {

  def selectDynamicImpl[A](name: Expr[String], self: Expr[SQLSyntaxSupportFeature#SQLSyntaxProvider[A]])(implicit qctx: Quotes, a: Type[A]): Expr[SQLSyntax] = {
    import qctx.reflect._
    val n: String = name.valueOrError

    val expectedNames: scala.collection.Set[String] = a.unseal.tpe.classSymbol.get.caseFields.map(_.name).toSet

    if (expectedNames.nonEmpty && !expectedNames.contains(n)) {
      error(s"${a.show}#${name.show} not found. Expected fields are ${expectedNames.mkString("#", ", #", "")}.", name.unseal.pos)
    }

    '{ ${self}.field(${name}) }
  }

}

