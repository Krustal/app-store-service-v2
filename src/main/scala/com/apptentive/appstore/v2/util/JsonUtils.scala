package com.apptentive.appstore.v2.util

import org.json4s.NoTypeHints
import org.json4s.native.JsonMethods.{compact, parse, render}
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write

object JsonUtils {
  implicit val formats = Serialization.formats(NoTypeHints)

  def jsonify(caseClassObj: AnyRef) = compact(render(parse(write(caseClassObj)).snakizeKeys))
}
