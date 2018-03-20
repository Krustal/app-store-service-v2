package com.apptentive.appstore.v2.model

case class Ratings(
  storeId: String,
  store: String,
  region: String,
  ingestTime: Long,
  storeObservedTime: Long,
  allRatings: Map[Int, Long],
  allRatingsAverage: Double,
  allRatingsCount: Long,
  currentRatings: Map[Int, Long],
  currentRatingsAverage: Double,
  currentRatingsCount: Long,
  version: String,
) extends Pageable {
  override def nextMinKey = ingestTime
}
