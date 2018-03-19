package com.apptentive.appstore.v2

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.apptentive.appstore.v2.api.{AppService, RatingsService}
import com.apptentive.appstore.v2.config.{CassandraConfig, ServerConfig}
import com.apptentive.appstore.v2.repository.{AppRepository, RatingsRepository}

import scala.concurrent.ExecutionContext
import akka.http.scaladsl.server.Directives._

object Server extends App {
  implicit val system = ActorSystem("app-store-service-v2")
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executionContext = ExecutionContext.global

  val cluster = CassandraConfig.defaultCluster
  val appService = new AppService(new AppRepository, cluster)
  val ratingsService = new RatingsService(new RatingsRepository, cluster)

  Http().bindAndHandle(appService.routes ~ ratingsService.routes, ServerConfig.host, ServerConfig.port)
}
