package com.apptentive.appstore.v2.model

object PaginationParams {
  val DEFAULT_PAGE_SIZE = 50
  val MAX_PAGE_SIZE = 5000
}

case class PaginationParams(private val _pageSize: Option[Int], minKey: Option[Long], sortOrder: Option[String]) {
  def pageSize = {
    Math.min(_pageSize.getOrElse(PaginationParams.DEFAULT_PAGE_SIZE), PaginationParams.MAX_PAGE_SIZE)
  }
}