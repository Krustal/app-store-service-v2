package com.apptentive.appstore.v2.util

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.{Date, TimeZone}

import akka.http.scaladsl.unmarshalling.Unmarshaller

object DateUtils {
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

  def parseDate(s: String) = {
    dateFormat.parse(s)
  }

  val dateUnmarshaller = Unmarshaller.strict[String, Date](s => {
    parseDate(s)
  })

  def toDateTime(date: Date) = {
    LocalDateTime.ofInstant(date.toInstant,
      TimeZone.getDefault.toZoneId)
  }
}
