package scalikejdbc.mapper

import java.sql.{ Types => JavaSqlTypes }

case class GeneratorConfig(
  srcDir: String = "src/main/scala",
  testDir: String = "src/test/scala",
  packageName: String = "models",
  template: GeneratorTemplate = GeneratorTemplate("queryDsl"),
  testTemplate: GeneratorTestTemplate = GeneratorTestTemplate(""),
  lineBreak: LineBreak = LineBreak("\n"),
  caseClassOnly: Boolean = false,
  encoding: String = "UTF-8",
  autoConstruct: Boolean = false,
  defaultAutoSession: Boolean = true,
  dateTimeClass: DateTimeClass = DateTimeClass.JodaDateTime,
  tableNameToClassName: String => String = GeneratorConfig.toCamelCase,
  columnNameToFieldName: String => String = GeneratorConfig.lowerCamelCase andThen GeneratorConfig.quoteReservedWord,
  returnCollectionType: ReturnCollectionType = ReturnCollectionType.List,
  view: Boolean = false,
  typeMapping: (String, Column, DateTimeClass) => Option[String] = GeneratorConfig.defaultTypeMapping
)

object GeneratorConfig {
  private def toProperCase(s: String): String = {
    import java.util.Locale.ENGLISH
    if (s == null || s.trim.size == 0) ""
    else s.substring(0, 1).toUpperCase(ENGLISH) + s.substring(1).toLowerCase(ENGLISH)
  }

  private val toCamelCase: String => String = _.split("_").foldLeft("") {
    (camelCaseString, part) =>
      camelCaseString + toProperCase(part)
  }

  val reservedWords: Set[String] = Set(
    "abstract", "case", "catch", "class", "def",
    "do", "else", "extends", "false", "final",
    "finally", "for", "forSome", "if", "implicit",
    "import", "lazy", "match", "new", "null", "macro",
    "object", "override", "package", "private", "protected",
    "return", "sealed", "super", "then", "this", "throw",
    "trait", "try", "true", "type", "val",
    "var", "while", "with", "yield"
  )

  val quoteReservedWord: String => String = {
    name =>
      if (reservedWords(name)) "`" + name + "`"
      else name
  }

  val lowerCamelCase: String => String =
    GeneratorConfig.toCamelCase.andThen {
      camelCase => camelCase.head.toLower + camelCase.tail
    }

  val defaultTypeMapping: (String, Column, DateTimeClass) => Option[String] = { (_, column, dateTimeClass) =>
    PartialFunction.condOpt(column.dataType) {
      case JavaSqlTypes.ARRAY => TypeName.AnyArray
      case JavaSqlTypes.BIGINT => TypeName.Long
      case JavaSqlTypes.BINARY => TypeName.ByteArray
      case JavaSqlTypes.BIT => TypeName.Boolean
      case JavaSqlTypes.BLOB => TypeName.Blob
      case JavaSqlTypes.BOOLEAN => TypeName.Boolean
      case JavaSqlTypes.CHAR => TypeName.String
      case JavaSqlTypes.CLOB => TypeName.Clob
      case JavaSqlTypes.DATALINK => TypeName.Any
      case JavaSqlTypes.DATE => TypeName.LocalDate
      case JavaSqlTypes.DECIMAL => TypeName.BigDecimal
      case JavaSqlTypes.DISTINCT => TypeName.Any
      case JavaSqlTypes.DOUBLE => TypeName.Double
      case JavaSqlTypes.FLOAT => TypeName.Float
      case JavaSqlTypes.INTEGER => TypeName.Int
      case JavaSqlTypes.JAVA_OBJECT => TypeName.Any
      case JavaSqlTypes.LONGVARBINARY => TypeName.ByteArray
      case JavaSqlTypes.LONGVARCHAR => TypeName.String
      case JavaSqlTypes.NULL => TypeName.Any
      case JavaSqlTypes.NUMERIC => TypeName.BigDecimal
      case JavaSqlTypes.OTHER => TypeName.Any
      case JavaSqlTypes.REAL => TypeName.Float
      case JavaSqlTypes.REF => TypeName.Ref
      case JavaSqlTypes.SMALLINT => TypeName.Short
      case JavaSqlTypes.STRUCT => TypeName.Struct
      case JavaSqlTypes.TIME => TypeName.LocalTime
      case JavaSqlTypes.TIMESTAMP => dateTimeClass.simpleName
      case JavaSqlTypes.TINYINT => TypeName.Byte
      case JavaSqlTypes.VARBINARY => TypeName.ByteArray
      case JavaSqlTypes.VARCHAR => TypeName.String
    }
  }
}
