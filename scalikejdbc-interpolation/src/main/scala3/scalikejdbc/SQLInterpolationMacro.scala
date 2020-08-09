package scalikejdbc

import scalikejdbc.interpolation.SQLSyntax
import scala.quoted._
import scalikejdbc.SQLSyntaxSupportFeature

/**
 * Macros for dynamic fields validation
 */
object SQLInterpolationMacro {

  def selectDynamicImpl[A](name: Expr[String], self: Expr[SQLSyntaxSupportFeature#SQLSyntaxProvider[A]])(implicit qctx: QuoteContext, a: Type[A]): Expr[SQLSyntax] = {
    import qctx.tasty._
    val n: String = name.unliftOrError
    
    val expectedNames: Set[String] = a.unseal.tpe.classSymbol.get.caseFields.map(_.name).toSet
    
    if (expectedNames.nonEmpty && !expectedNames.contains(n)) {
      error(s"${a.show}#${name.show} not found. Expected fields are ${expectedNames.mkString("#", ", #", "")}.", name.unseal.pos)
    }

    '{ ${self}.field(${name}) }
  }

}

