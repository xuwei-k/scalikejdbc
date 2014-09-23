/*
 * Copyright 2012 Kazuhiro Sera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package scalikejdbc.mapper

case class GeneratorConfig(srcDir: String = "src/main/scala",
  testDir: String = "src/test/scala",
  packageName: String = "models",
  template: GeneratorTemplate = GeneratorTemplate.queryDsl,
  testTemplate: Option[GeneratorTestTemplate] = None,
  lineBreak: LineBreak = LineBreak("\n"),
  caseClassOnly: Boolean = false,
  encoding: String = "UTF-8",
  autoConstruct: Boolean = false,
  defaultAutoSession: Boolean = true)

object GeneratorTemplate {
  private[this] val map = List(interpolation, queryDsl).map {
    template => template.name -> template
  }.toMap

  def apply(name: String): Option[GeneratorTemplate] = map.get(name)

  object interpolation extends GeneratorTemplate("interpolation")
  object queryDsl extends GeneratorTemplate("queryDsl")
}

sealed abstract class GeneratorTemplate(private[scalikejdbc] val name: String)

object GeneratorTestTemplate {

  private[this] val map = List(ScalaTestFlatSpec, specs2unit, specs2acceptance).map {
    template => template.name -> template
  }.toMap

  def apply(name: String): Option[GeneratorTestTemplate] = map.get(name)

  object ScalaTestFlatSpec extends GeneratorTestTemplate("ScalaTestFlatSpec")
  object specs2unit extends GeneratorTestTemplate("specs2unit")
  object specs2acceptance extends GeneratorTestTemplate("specs2acceptance")
}
sealed abstract class GeneratorTestTemplate(private[scalikejdbc] val name: String)

object LineBreak {
  def value(name: String) = name match {
    case "CR" => "\r"
    case "LF" => "\n"
    case "CRLF" => "\r\n"
    case _ => "\n"
  }
}

case class LineBreak(name: String) {
  def value = LineBreak.value(name)
}

