package com.apptentive.appstore.v2.api

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import com.apptentive.appstore.v2.model.{AppStore, EmptyResult, PaginationParams, RatingsQueryParameters}
import com.apptentive.appstore.v2.repository.RatingsRepository
import com.apptentive.appstore.v2.util.DateUtils.dateUnmarshaller
import com.apptentive.appstore.v2.util.JsonUtils.jsonify
import com.datastax.driver.core.Cluster
import com.typesafe.scalalogging.LazyLogging

class RatingsService(ratingsRepository: RatingsRepository, cluster: Cluster) extends LazyLogging {

  import akka.http.scaladsl.server.Directives._

  val routes = {
    implicit val session = cluster.connect()

    pathPrefix("api" / "v2") {
      parameters('page_size.as[Int].?, 'min_key.as[Long].?, Symbol("start_date").as(dateUnmarshaller).?, Symbol("end_date").as(dateUnmarshaller).?, 'sort_order.as[String].?) { (pageSize, minKey, startDate, endDate, sortOrder) =>
        val queryParams = RatingsQueryParameters(startDate, endDate)
        val paginationParams = PaginationParams(pageSize, minKey, sortOrder)
        path("store" / Segment.flatMap(AppStore.fromPath) / "apps" / Segment / "ratings-histograms") { (store, storeId) =>
          get {
            val ratings = ratingsRepository.getRatings(store, storeId, paginationParams, queryParams)
            complete(HttpEntity(ContentTypes.`application/json`, jsonify(ratings)))
          }
        } ~
          path("store" / Segment.flatMap(AppStore.fromPath) / "apps" / Segment / "ratings-histograms" / Segment) { (store, storeId, date) =>
            get {
              val rating = ratingsRepository.getRatingsByDate(store, storeId, date)
              rating match {
                case Some(value) => complete(HttpEntity(ContentTypes.`application/json`, jsonify(rating.getOrElse(EmptyResult))))
                case None => complete(HttpResponse(StatusCodes.NotFound, entity = HttpEntity(ContentTypes.`application/json`, "")))
              }
            }
          }
      }
    }
  }
}
