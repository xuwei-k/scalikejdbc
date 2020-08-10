package scalikejdbc

import scala.quoted._

object autoColumns {

  def apply_impl[A](
    excludes: Expr[Seq[String]]
  )(implicit qctx: QuoteContext, tpe: Type[A]): Expr[Seq[String]] = {
    import qctx.tasty._
    
    val nameConverters: Symbol = {
      def loop(owner: qctx.tasty.Symbol): Symbol = {
        owner.method("nameConverters").headOption match {
          case Some(x) =>
            x
          case None =>
            loop(owner.owner)
        }
      }

      loop(rootContext.owner)
    }
    println(nameConverters)
    /*
    val columns = EntityUtil.constructorParams[A]("autoColumns", excludes: _*).map { field =>
      '{
      scalikejdbc.autoColumns.camelToSnake(
        ???,
        ???,
        ???
      )
      }
    }
    
    sequence(columns)
     */
    ???
  }

  private def sequence[X: Type](xs: Seq[Expr[X]])(using quoteContext: QuoteContext): Expr[Seq[X]] = {
    xs match {
      case h +: t  =>
        '{ $h +: ${ sequence(t) }  }
      case _ =>
        '{ Seq.empty[X] }
    }
  }


  def camelToSnake(fieldName: String, nameConverters: Map[String, String], useSnakeCase: Boolean): String = {
    SQLSyntaxProvider.toColumnName(fieldName, nameConverters, useSnakeCase)
  }

  inline def apply[A](excludes: String*): collection.Seq[String] =
    ${ autoColumns.apply_impl[A]('{excludes}) }

}
