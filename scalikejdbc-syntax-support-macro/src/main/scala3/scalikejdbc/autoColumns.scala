package scalikejdbc

import scala.quoted._

object autoColumns {

  def apply_impl[A](excludes: Expr[String]*)(implicit qctx: QuoteContext, tpe: Type[A]): Expr[Seq[String]] = {
    import qctx.tasty._
    
    val nameConverters: Symbol = rootContext.owner.field("nameConverters")
    val columns = EntityUtil.constructorParams[A]("autoColumns", excludes: _*).map { field =>
     '{
        scalikejdbc.autoColumns.camelToSnake(
          field.name,
          ${nameConverters.tree}.cast[Map[String, String]],
          ${Ident(bb).seal.cast[Boolean]},
        ) 
      }
    }
    columns
  }

  def camelToSnake(fieldName: String, nameConverters: Map[String, String], useSnakeCase: Boolean): String = {
    SQLSyntaxProvider.toColumnName(fieldName, nameConverters, useSnakeCase)
  }

  def debug_impl[A](excludes: Expr[String]*)(using qctx: QuoteContext, tpe: Type[A]): Expr[Seq[String]] = {
    val expr = apply_impl[A](excludes: _*)
    println(expr.show)
    expr
  }

  inline def apply[A](inline excludes: String*): collection.Seq[String] = '{ autoColumns.apply_impl[A] }

  inline def debug[A](inline excludes: String*): collection.Seq[String] = '{ autoColumns.debug_impl[A] }

}
