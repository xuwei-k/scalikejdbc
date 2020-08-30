package scalikejdbc

import scalikejdbc.SQLSyntaxSupportFeature
import scalikejdbc.interpolation.SQLSyntax

trait SelectDynamicMacro[A] { self: SQLSyntaxSupportFeature#SQLSyntaxProvider[A] =>
  inline def selectDynamic(name: String): SQLSyntax =
    ${ scalikejdbc.SQLInterpolationMacro.selectDynamicImpl('{name}, '{this}) }
}
