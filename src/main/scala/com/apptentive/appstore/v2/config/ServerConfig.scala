package com.apptentive.appstore.v2.config

object ServerConfig {
  def host: String = {
    sys.props.getOrElse("SERVER_HOST", "") match {
      case "" => "0.0.0.0"
      case other => other
    }
  }

  def port: Int = {
    sys.props.getOrElse("SERVER_PORT", "") match {
      case "" => 8080
      case other => other.toInt
    }
  }
}
