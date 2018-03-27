package com.apptentive.appstore.v2.api

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import com.apptentive.appstore.v2.elasticsearch.ESClient
import com.apptentive.appstore.v2.model.{AppQueryParameters, AppStore, EmptyResult, PaginationParams}
import com.apptentive.appstore.v2.repository.AppRepository
import com.apptentive.appstore.v2.util.DateUtils.dateUnmarshaller
import com.apptentive.appstore.v2.util.JsonUtils.jsonify
import com.datastax.driver.core.Cluster
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class AppService(appRepository: AppRepository, eSClient: ESClient, cluster: Cluster) extends LazyLogging {

  import akka.http.scaladsl.server.Directives._

  val routes = {
    implicit val session = cluster.connect()

    pathPrefix("api" / "v2") {
      parameters('page_size.as[Int].?, 'min_key.as[Long].?, Symbol("as-of").as(dateUnmarshaller).?) { (pageSize, minKey, asOf) =>
        val queryParams = AppQueryParameters(asOf)
        val paginationParams = PaginationParams(pageSize, minKey, None)
        path("store" / "apps") {
          parameters('text, 'per.as[Int] ? 10) { (text, perParam) =>
            val per = Math.min(perParam, 50)
            get {
              val resultFuture = eSClient.searchApps(text, per)
              onComplete(resultFuture) {
                case Success(result) => complete(HttpEntity(ContentTypes.`application/json`, jsonify(result)))
                case Failure(ex) => complete(HttpResponse(StatusCodes.InternalServerError))
              }
            }
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
