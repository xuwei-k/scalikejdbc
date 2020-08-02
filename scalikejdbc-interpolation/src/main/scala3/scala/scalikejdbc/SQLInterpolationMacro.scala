package scalikejdbc

import scalikejdbc.interpolation.SQLSyntax
import scala.quoted._
import scalikejdbc.SQLSyntaxSupportFeature

/**
 * Macros for dynamic fields validation
 */
object SQLInterpolationMacro {

  def selectDynamicImpl(name: Expr[String], self: Expr[SQLSyntaxProvider[_]])(implicit qctx: QuoteContext): Expr[SQLSyntax] = {
    import qctx.tasty._
    val n: String = name.unliftOrError

    //    qctx.tasty.getClass.getMethods.foreach(println)
    //    println(qctx.tasty.rootContext.owner.tree)
    //   println(qctx.tasty.rootContext.owner.owner)
    //println(qctx.tasty.rootContext.owner.owner.owner.tree.getClass)

    def getClassDef(s: Symbol): ClassDef = {
      val s2 = s.owner
      s2.tree match {
        case t: ClassDef =>
          t
        case _ =>
          getClassDef(s2)
      }
    }
    val clazz = getClassDef(rootContext.owner)
    val parents = clazz.parents
    val List(typeParam) = parents.map(_.asInstanceOf[TypeTree].tpe).collect {
      case AppliedType(a, List(x)) if a.classSymbol.map(_.fullName) == Some("scalikejdbc.SQLSyntaxSupport") =>
        x
    }.collect{
      case x @ TypeRef(_, _) => x
    }
    val expectedNames = typeParam.classSymbol.get.caseFields.map(_.name).toSet
    
    //println(expectedNames)
    if (expectedNames(n)) {
      '{ ${self}.field(${name}) }
    } else {
      error(s"${typeParam}#${name} not found. Expected fields are ${expectedNames.mkString("#", ", #", "")}.", name.unseal.pos)
      '{???}
    }

    /*
    // primary constructor args of type E
    val expectedNames = c.weakTypeOf[E].decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }.map { const =>
      const.paramLists.flatMap { _.map(_.name.encodedName.toString.trim) }
    }.getOrElse(Nil)

    nameOpt.map { _name =>
      if (expectedNames.nonEmpty && !expectedNames.contains(_name)) {
        c.error(c.enclosingPosition, s"${c.weakTypeOf[E]}#${_name} not found. Expected fields are ${expectedNames.mkString("#", ", #", "")}.")
      }
    }

    Apply(Select(c.prefix.tree, TermName("field")), List(name))
*/
  }

}

