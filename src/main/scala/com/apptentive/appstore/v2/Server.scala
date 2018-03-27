package com.apptentive.appstore.v2

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.apptentive.appstore.v2.api.{AppService, RatingsService}
import com.apptentive.appstore.v2.config.{CassandraConfig, ServerConfig}
import com.apptentive.appstore.v2.elasticsearch.ESClient
import com.apptentive.appstore.v2.repository.{AppRepository, RatingsRepository}

object Server extends App {
  implicit val system = ActorSystem("app-store-service-v2")
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val cluster = CassandraConfig.defaultCluster
  val appService = new AppService(new AppRepository, new ESClient, cluster)
  val ratingsService = new RatingsService(new RatingsRepository, cluster)

  Http().bindAndHandle(appService.routes ~ ratingsService.routes, ServerConfig.host, ServerConfig.port)
}
