package scalikejdbc

import scalikejdbc.interpolation.SQLSyntax

trait SelectDynamicMacro[A] {
  inline def selectDynamic(name: String): SQLSyntax =
    ${ scalikejdbc.SQLInterpolationMacro.selectDynamic[A]('{name}) }
}
