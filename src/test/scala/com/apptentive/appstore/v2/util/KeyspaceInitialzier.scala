package com.apptentive.appstore.v2.util

import java.nio.charset.StandardCharsets

import com.apptentive.appstore.v2.config.CassandraConfig
import com.datastax.driver.core.Session

import scala.io.Source

object KeyspaceInitialzier {
  def setupKeyspace()(implicit session: Session) = {
    session.execute(Source.fromFile("src/test/resources/v2/create_keyspace.cql")
      .mkString.replace("_keyspace", CassandraConfig.keyspace))

    session.execute(Source.fromFile("src/test/resources/v2/create_apps.cql")
      .mkString.replace("_keyspace", CassandraConfig.keyspace))

    session.execute(Source.fromFile("src/test/resources/v2/create_current_version_apps.cql")
      .mkString.replace("_keyspace", CassandraConfig.keyspace))

    session.execute(Source.fromFile("src/test/resources/v2/create_ratings.cql")
      .mkString.replace("_keyspace", CassandraConfig.keyspace))

    Source.fromFile("src/test/resources/v2/insert_current_version_apps.cql", StandardCharsets.UTF_8.name()).getLines().foreach(l =>
      session.execute(l.replaceAll("_keyspace", CassandraConfig.keyspace))
    )

    Source.fromFile("src/test/resources/v2/insert_apps.cql", StandardCharsets.UTF_8.name()).getLines().foreach(l =>
      session.execute(l.replaceAll("_keyspace", CassandraConfig.keyspace))
    )

    Source.fromFile("src/test/resources/v2/insert_ratings.cql", StandardCharsets.UTF_8.name()).getLines().foreach(l =>
      session.execute(l.replaceAll("_keyspace", CassandraConfig.keyspace))
    )

    session.execute("USE " + CassandraConfig.keyspace)
  }
}
