package com.apptentive.appstore.v2.elasticsearch

/**
  * Elastic Search Config.
  */
object ElasticConfig {
  def uri: String = {
    sys.env.getOrElse("ELASTIC_SEARCH_URI", "") match {
      case "" => "http://localhost:9200"
      case other => other
    }
  }
}
