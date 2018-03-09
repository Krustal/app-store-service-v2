package com.apptentive.appstore.v2.model

import java.time.{LocalDateTime, ZoneOffset}

case class Ratings(
  storeId: String,
  store: String,
  region: String,
  ingestTime: LocalDateTime,
  storeObservedTime: LocalDateTime,
  allRatings: Map[Int, Long],
  allRatingsAverage: Double,
  allRatingsCount: Long,
  currentRatings: Map[Int, Long],
  currentRatingsAverage: Double,
  currentRatingsCount: Long,
  version: String,
) extends Pageable {
  override def nextMinKey = ingestTime.toEpochSecond(ZoneOffset.UTC)
}
