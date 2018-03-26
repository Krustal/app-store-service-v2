package com.apptentive.appstore.v2.api

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import com.apptentive.appstore.v2.model.{AppQueryParameters, AppStore, EmptyResult, PaginationParams}
import com.apptentive.appstore.v2.repository.AppRepository
import com.apptentive.appstore.v2.util.DateUtils.dateUnmarshaller
import com.apptentive.appstore.v2.util.JsonUtils.jsonify
import com.datastax.driver.core.Cluster
import com.typesafe.scalalogging.LazyLogging

class AppService(appRepository: AppRepository, cluster: Cluster) extends LazyLogging {

  import akka.http.scaladsl.server.Directives._

  val routes = {
    implicit val session = cluster.connect()

    pathPrefix("api" / "v2") {
      parameters('page_size.as[Int].?, 'min_key.as[Long].?, Symbol("as-of").as(dateUnmarshaller).?) { (pageSize, minKey, asOf) =>
        val queryParams = AppQueryParameters(asOf)
        val paginationParams = PaginationParams(pageSize, minKey, None)
        path("store" / Segment.flatMap(AppStore.fromPath) / "apps") { store =>
          get {
            val result = appRepository.findByStore(store, paginationParams, queryParams)
            complete(HttpEntity(ContentTypes.`application/json`, jsonify(result)))
          }
        } ~
          path("store" / Segment.flatMap(AppStore.fromPath) / "apps" / Segment) { (store, storeId) =>
            get {
              val result = appRepository.findApp(store, storeId)
              if (result.isDefined)
                complete(HttpEntity(ContentTypes.`application/json`, jsonify(result)))
              else
                complete(HttpResponse(StatusCodes.NotFound))
            }
          } ~
          path("store" / Segment.flatMap(AppStore.fromPath) / "apps" / Segment / "versions") { (store, storeId) =>
            get {
              val result = appRepository.findVersions(store, storeId, PaginationParams(pageSize, minKey, None), queryParams)
              complete(HttpEntity(ContentTypes.`application/json`, jsonify(result)))
            }
          } ~
          path("store" / Segment.flatMap(AppStore.fromPath) / "apps" / Segment / "versions" / Segment) { (store, storeId, version) =>
            get {
              val result = appRepository.findVersion(store, storeId, version)
              complete(HttpEntity(ContentTypes.`application/json`, jsonify(result.getOrElse(EmptyResult))))
            }
          } ~
          path("health") {
            get {
              complete("OK")
            }
          }
      }
    }
  }
}
