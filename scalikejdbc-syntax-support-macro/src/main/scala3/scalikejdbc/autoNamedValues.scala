package scalikejdbc

import scala.deriving.Mirror
import scala.compiletime.{constValue, constValueTuple, erasedValue, summonFrom, summonInline}
import scala.reflect.Selectable.reflectiveSelectable

object autoNamedValues {
  def debug[E](entity: E, column: ColumnName[E], excludes: String*): Map[SQLSyntax, ParameterBinder] = ???

  inline def apply[E](entity: E, column: ColumnName[E], inline excludes: String*): Map[SQLSyntax, ParameterBinder] =
    summonFrom {
      case _: Mirror.ProductOf[E] =>
        applyImpl[E](entity, column, excludes: _*)
    }
    
  type ParameterBinderFactories[A <: Tuple] = Tuple.Map[A, ParameterBinderFactory] 

  inline def summonParameterBinderFactoryRec[T <: Tuple]: List[ParameterBinderFactory[_]] =
    inline erasedValue[T] match {
      case _: EmptyTuple =>
        Nil
      case _: (t *: ts) =>
        summonParameterBinderFactory[t] :: summonParameterBinderFactoryRec[ts]
    }

  inline def summonParameterBinderFactory[A]: ParameterBinderFactory[A] =
    summonInline[ParameterBinderFactory[A]]

  inline def applyImpl[E](
    entity: E,
    column: ColumnName[E],
    inline excludes: String*
  )(using inline E: Mirror.ProductOf[E]): Map[SQLSyntax, ParameterBinder] = {
    val excludesSet = excludes.toSet
    val xxx = constValue[E.MirroredLabel]
    val labels: List[String] = constValueTuple[E.MirroredElemLabels].toList.asInstanceOf[List[String]].filterNot(excludes.toSet)
    val parameterBinderFactories = summonParameterBinderFactoryRec[E.MirroredElemTypes]
    labels.zip(parameterBinderFactories).map{ case (label, f) =>
      Tuple2(
        column.field(label),
        f.asInstanceOf[ParameterBinderFactory[Any]].apply(
          entity.selectDynamic(label).asInstanceOf[Any]
        )
      )
    }.toMap
  }
}
