package scalikejdbc

import scala.deriving.Mirror
import scala.compiletime.{constValue, erasedValue, summonFrom}

object SQLSyntaxSupportFactory {
  inline def apply[A](inline excludes: String*): SQLSyntaxSupportImpl[A] =
    applyImpl[A](excludes: _*)

  inline def applyImpl[A](inline excludes: String*): SQLSyntaxSupportImpl[A] =
    summonFrom {
      case _: Mirror.ProductOf[A] =>
        applyImpl0[A](excludes: _*)
    }

  def camelToSnake(className: String): String = {
    val clazz = className.replaceFirst("\\$$", "").replaceFirst("^.+\\.", "").replaceFirst("^.+\\$", "")
    SQLSyntaxProvider.toColumnName(clazz, Map.empty, true)
  }

  inline def applyImpl0[A](
    inline excludes: String*
  )(using inline A: Mirror.ProductOf[A]): SQLSyntaxSupportImpl[A] = {
    val excludesSet = excludes.toSet
    val labels = EntityUtil.summonLabelsRec[A.MirroredElemLabels].filterNot(excludes.toSet)
    val typeBinders = EntityUtil.summonTypeBinders[A.MirroredElemTypes]
    new scalikejdbc.SQLSyntaxSupportImpl[A] {
      override val tableName: String = {
        println(A.getClass)
        SQLSyntaxSupportFactory.camelToSnake(A.getClass.getSimpleName)
      }
      override lazy val columns: collection.Seq[String] =
        autoColumns.apply[A](excludes: _*)
      def apply(rn: ResultName[A])(rs: WrappedResultSet): A = {
        val values = labels.zip(typeBinders).map{
          (label, typeBinder) =>
            rs.get[AnyRef](rn.field(label))(EntityUtil.cast(typeBinder))
        }
        A.fromProduct(new Product{
          override def canEqual(that: Any): Boolean =
            true
          override def productArity: Int =
            values.length
          override def productElement(n: Int): Any =
            values(n)
        })
      }
    }
  }

}
