package com.apptentive.appstore.v2.api

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import com.apptentive.appstore.v2.config.CassandraConfig
import com.apptentive.appstore.v2.model.{AppQueryParameters, AppStore, EmptyResult, PaginationParams}
import com.apptentive.appstore.v2.repository.AppRepository
import com.apptentive.appstore.v2.util.DateUtils.dateUnmarshaller
import com.apptentive.appstore.v2.util.JsonUtils.jsonify
import com.typesafe.scalalogging.LazyLogging
import org.json4s._
import org.json4s.native.Serialization

class AppService(appRepository: AppRepository) extends LazyLogging {

  import akka.http.scaladsl.server.Directives._

  val routes = {
    val cluster = CassandraConfig.defaultCluster
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
          }
      }
    }
  }
}
