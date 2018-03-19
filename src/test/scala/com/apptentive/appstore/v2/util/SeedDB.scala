package com.apptentive.appstore.v2.util

import com.apptentive.appstore.v2.config.CassandraConfig

object SeedDB extends App {
  val cluster =
    CassandraConfig.defaultCluster

  implicit val session = cluster.connect()

  KeyspaceInitialzier.setupKeyspace()
}
