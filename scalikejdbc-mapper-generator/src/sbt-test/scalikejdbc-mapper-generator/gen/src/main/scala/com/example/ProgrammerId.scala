package com.example

import scalikejdbc._

final case class ProgrammerId(value: Int)

object ProgrammerId {
  implicit val binders: Binders[ProgrammerId] =
    Binders.int.xmap(apply, _.value)
}
