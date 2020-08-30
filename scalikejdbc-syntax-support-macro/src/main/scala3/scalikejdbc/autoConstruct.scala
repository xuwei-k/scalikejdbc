package scalikejdbc

import scala.compiletime.{constValue, erasedValue, summonFrom, summonInline}
import scala.deriving._

object autoConstruct {
  inline def apply[A](rs: WrappedResultSet, rn: ResultName[A], inline excludes: String*): A =
    summonFrom {
      // TODO support non case class
      case _: Mirror.ProductOf[A] =>
        applyImpl[A](rs, rn, excludes: _*)
    }

  inline def applyImpl[A](rs: WrappedResultSet, rn: ResultName[A], inline excludes: String*)(using inline A: Mirror.ProductOf[A]): A = {
    // TODO exclude
    val labels: Array[String] = EntityUtil.summonLabels[A.MirroredElemLabels].toArray
    val binders = EntityUtil.summonTypeBinders[A.MirroredElemTypes].toArray
    val values = labels.zip(binders).map{
      case (label, t) =>
        rs.get[AnyRef](rn.field(label))(EntityUtil.cast(t))
    }

    A.fromProduct(new ArrayProduct(values))
  }
  
  inline def apply[A](rs: WrappedResultSet, sp: SyntaxProvider[A], inline excludes: String*): A =
    apply[A](rs, sp.resultName, excludes: _*)

  def debug[A](rs: WrappedResultSet, rn: ResultName[A], excludes: String*): A = ???

  def debug[A](rs: WrappedResultSet, sp: SyntaxProvider[A], excludes: String*): A = ???
}
