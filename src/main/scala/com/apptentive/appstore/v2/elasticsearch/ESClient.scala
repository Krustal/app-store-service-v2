package com.apptentive.appstore.v2.elasticsearch

import com.apptentive.appstore.v2.model.AppSuggestion
import com.google.gson.internal.LinkedTreeMap
import com.typesafe.scalalogging.LazyLogging
import io.searchbox.client.JestClientFactory
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.core.Suggest

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class ESClient(implicit ec: ExecutionContext) extends LazyLogging {

  private val appIndex = "004-store_apps_v2"
  private val store_app = "store_app"

  val client = {
    val factory = new JestClientFactory()
    val httpConfig = new HttpClientConfig.Builder(ElasticConfig.uri)
      .multiThreaded(true)
      .readTimeout(20000)
      .maxTotalConnection(100)
      .build()
    factory.setHttpClientConfig(httpConfig)
    factory.getObject
  }

  def searchApps(text: String, per: Int): Future[List[AppSuggestion]] = {
    val query =
      s"""{
            "text": "${text}",
            "suggestion": {
              "completion": {
                "size": ${per},
                "field": "app_name",
                "context": {
                  "store": null
                }
              }
            }
          }"""

    val suggestResult = Future(client.execute(new Suggest.Builder(query).addIndex(appIndex).build()))

    suggestResult.map(result => result.getSuggestions("suggestion").asScala(0).options.asScala.map(suggestion => parse(suggestion.get("payload").asInstanceOf[LinkedTreeMap[String, String]])).toList)
  }

  private def parse(value: LinkedTreeMap[String, String]): AppSuggestion = {
    AppSuggestion(
      value.get("store_app_id"),
      value.get("store"),
      value.get("publisher_name"),
      value.get("publisher_id"),
      value.get("app_name")
    )
  }
}
