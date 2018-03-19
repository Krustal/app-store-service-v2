package com.apptentive.appstore.v2.model

case class App(storeId: String,
               store: String,
               category: String,
               developer: String,
               title: String,
               icon: String,
               version: String,
               updatedDate: Long,
               ingestionTime: Long) extends Pageable {
  override def nextMinKey: Long = updatedDate
}