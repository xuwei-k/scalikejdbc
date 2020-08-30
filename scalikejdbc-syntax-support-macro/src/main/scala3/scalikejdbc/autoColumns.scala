package scalikejdbc

import scala.quoted._
import scala.compiletime._
import scala.deriving._

object autoColumns {
  
  inline def applyImpl[A1](inline excludes: String*)(using A1: Mirror.ProductOf[A1])(using companion: SQLSyntaxSupport[A1]): collection.Seq[String] = {
    /*
      val columns = EntityUtil.constructorParams[A](c)("autoColumns", excludes: _*).map { field =>
      q"scalikejdbc.autoColumns.camelToSnake(${field.name.decodedName.toString}, nameConverters, useSnakeCaseColumnName)"
      }
      c.Expr[Seq[String]](q"Seq(..$columns)")
     */
//    EntityUtil.summonLabels[A1.MirroredElemLabels]
    val excludeSet = excludes.toSet
    EntityUtil.cast(constValueTuple[A1.MirroredElemLabels].toList).filterNot(excludeSet).map{ field =>
      SQLSyntaxProvider.toColumnName(field, companion.nameConverters, companion.useSnakeCaseColumnName)
    }
  }

  inline def apply[A1: SQLSyntaxSupport](inline excludes: String*): collection.Seq[String] =
    summonFrom {
      case _: Mirror.ProductOf[A1] =>
        applyImpl[A1](excludes: _*)
    }

}
