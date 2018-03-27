package com.apptentive.appstore.v2.util

import com.apptentive.appstore.v2.config.CassandraConfig

import scala.util.Try

object SeedDB extends App {
  val cluster =
    CassandraConfig.defaultCluster

  implicit val session = cluster.connect()

  Try(session.execute("DROP KEYSPACE " + CassandraConfig.keyspace))

  KeyspaceInitialzier.setupKeyspace()

  session.close()

  cluster.close()
}
