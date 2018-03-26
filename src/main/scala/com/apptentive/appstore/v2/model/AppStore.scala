package com.apptentive.appstore.v2.model

object AppStore {
  def fromPath(path: String) = {
    Some(AppStore(path))
  }
}

case class AppStore(store: String) {
  require(store == "android" || store == "itunes")
}
