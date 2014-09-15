package scalikejdbc

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import scalikejdbc.MacroCompatible._

object autoSave {

  def autoSaveImpl[A: c.WeakTypeTag](c: Context)(entity: c.Expr[A], pkColumns: c.Expr[String]*)(session: c.Expr[DBSession]): c.Expr[A] = {
    import c.universe._

    val params: List[Tree] = autoConstruct.constructorParams[A](c)().map { field =>
      val name = field.name.decodedName.toTermName
      q"column.$name -> $entity.$name"
    }

    val t = q" update(this: _root_.scalikejdbc.SQLSyntaxSupport[_]).set(..$params) "

    val withWhere = pkColumns.map(_.tree).map {
      case q"${ pk: String }" =>
        val n = newTermName(pk)
        (q"column.$n", q"$entity.$n")
      case _ =>
        c.abort(c.enclosingPosition, "pkColumns must be String literal")
    }.toList match {
      case Nil =>
        c.abort(c.enclosingPosition, "pkColumns is empty")
      case (a, b) :: tail =>
        tail.foldLeft(q"$t.where.eq($a, $b)") {
          case (x, (y, z)) =>
            q"$x.and.eq($y, $z)"
        }
    }

    val tree = q" withSQL{$withWhere}.update.apply(); $entity"
    //    println(showCode(tree))
    c.Expr[A](tree)
  }

  def apply[A](entity: A, pkColumns: String*)(implicit session: DBSession): A = macro autoSaveImpl[A]

}
