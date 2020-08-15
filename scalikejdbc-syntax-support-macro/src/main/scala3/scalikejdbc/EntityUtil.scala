package scalikejdbc

import scala.quoted._

private[scalikejdbc] object EntityUtil {

  private[scalikejdbc] def constructorParams[A: Type](macroName: String, excludes: Expr[String]*)(using qctx: QuoteContext, tpe: Type[A]): List[qctx.tasty.Symbol] = {
    import qctx.tasty._
    val paramStrs = tpe.unseal.tpe.classSymbol.get.caseFields.map(_.name).toSet
    val params = tpe.unseal.tpe.classSymbol.get.caseFields
    val excludeStrs = excludes.map(_.unliftOrError).toSet
    excludeStrs.foreach { ex =>
      if (!paramStrs(ex)) {
        error(s"$ex does not found in ${tpe.show}", qctx.tasty.rootPosition)
      }
    }
    params.filterNot(p => excludeStrs(p.name))
  }

}