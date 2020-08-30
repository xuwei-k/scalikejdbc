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

  private[scalikejdbc] def constructorParams[A: Type](macroName: String, excludes: Expr[String]*)(using qctx: Quotes, tpe: Type[A]): List[qctx.reflect.Symbol] = {
    import qctx.reflect._
    val paramStrs = tpe.unseal.tpe.classSymbol.get.caseFields.map(_.name).toSet
    val params = tpe.unseal.tpe.classSymbol.get.caseFields
    val excludeStrs = excludes.map(_.unliftOrError).toSet
    excludeStrs.foreach { ex =>
      if (!paramStrs(ex)) {
        error(s"$ex does not found in ${tpe.show}", qctx.reflect.rootPosition)
      }
    }
    params.filterNot(p => excludeStrs(p.name))
  }

}
