package com.apptentive.appstore.v2.model

object AppStore {
  def fromPath(path: String) = {
    Some(AppStore(path))
  }
}

case class AppStore(private val _store: String) {
  require(_store == "android" || _store == "itunes")

  def store = {
    _store match {
      case "android" => "Android"
      case "itunes" => "iOS"
      case _ => throw new IllegalStateException
    }
  }
}
