package scalikejdbc

import scala.quoted._
import scala.compiletime.{constValue, erasedValue, summonFrom}

object EntityUtil {
  inline def cast[F[_], A, B](a: F[A]): F[B] = a.asInstanceOf[F[B]]

  inline def summonLabels[T <: Tuple]: Array[String] =
    summonLabelsRec[T].toArray

  inline def summonLabelsRec[T <: Tuple]: List[String] =
    inline erasedValue[T] match {
      case _: EmptyTuple =>
        Nil
      case _: (t *: ts) =>
        constValue[t].asInstanceOf[String] :: summonLabelsRec[ts]
    }

  inline def summonTypeBinder[A]: TypeBinder[A] =
    summonFrom {
      case x: TypeBinder[A] =>
        x
    }

  inline def summonTypeBinders[T <: Tuple]: List[TypeBinder[_]] =
    inline erasedValue[T] match {
      case _: EmptyTuple =>
        Nil
      case _: (t *: ts) =>
        summonTypeBinder[t] :: summonTypeBinders[ts]
    }

  private[scalikejdbc] def constructorParams[A: Type](macroName: String, excludes: Expr[String]*)(using qctx: Quotes): List[qctx.reflect.Symbol] = {
    import qctx.reflect._
    val typeSymbol = TypeRepr.of[A].typeSymbol
    val params = typeSymbol.caseFields
    val S = summon[FromExpr[String]]
    val paramStrs = params.map(_.name).toSet
    val excludeStrs = excludes.map{case S(s) => s}.toSet
    excludeStrs.foreach { ex =>
      if (!paramStrs(ex)) {
        report.error(s"$ex does not found in ${typeSymbol.fullName}", excludes.head.asTerm.pos)
      }
    }
    params.filterNot(p => excludeStrs(p.name))
  }

}
