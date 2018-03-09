package com.apptentive.appstore.v2.model

import java.time.{LocalDateTime, ZoneOffset}

case class App(storeId: String,
               store: String,
               category: String,
               developer: String,
               title: String,
               icon: String,
               version: String,
               updatedDate: LocalDateTime,
               ingestionTime: LocalDateTime) extends Pageable {
  override def nextMinKey: Long = updatedDate.toEpochSecond(ZoneOffset.UTC)
}