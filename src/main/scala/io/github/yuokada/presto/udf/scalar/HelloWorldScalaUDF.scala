package io.github.yuokada.presto.udf.scalar

import io.airlift.slice.Slice
import io.airlift.slice.Slices.utf8Slice
import io.prestosql.spi.`type`.StandardTypes
import io.prestosql.spi.function.{Description, ScalarFunction, SqlNullable, SqlType}

object HelloWorldScalaUDF {

  @Description("Hello World(UDF Practice)")
  @ScalarFunction("hello_worlds")
  @SqlType(StandardTypes.VARCHAR)
  def helloworld(@SqlNullable @SqlType(StandardTypes.VARCHAR) name: Slice): Slice =
    if (name == null || name.toStringUtf8.isEmpty) {
      utf8Slice("Hello World from scala")
    } else {
      utf8Slice(String.format("Hello %s from scala", name.toStringUtf8))
    }

  @Description("Apache Hive like translate(UDF Practice)")
  @ScalarFunction("translate")
  @SqlType(StandardTypes.VARCHAR)
  def translate(@SqlNullable @SqlType(StandardTypes.VARCHAR) target: Slice,
                @SqlType(StandardTypes.VARCHAR) from: Slice,
                @SqlType(StandardTypes.VARCHAR) to: Slice): Slice = {
    if (target == null || target.toStringUtf8.isEmpty) {
      target
    } else {
      val fromUtf8 = from.toStringAscii
      val converter = fromUtf8.distinct.zip(to.toStringAscii).toMap
      val excludes = if (to.length <= fromUtf8.length) {
        fromUtf8.substring(to.length).toSet
      } else {
        "".toSet
      }
      utf8Slice(
        target.toStringAscii.map {
          case c if converter.contains(c) => converter(c)
          case c if excludes.contains(c) => ""
          case c => c
        } .mkString
      )
    }
  }
}
