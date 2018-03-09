package com.apptentive.appstore.v2.api

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.apptentive.appstore.v2.config.CassandraConfig
import com.datastax.driver.core.{Cluster, Session}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSpec, Matchers}
import org.scalatest.mockito.MockitoSugar

import scala.io.Source

trait BaseCassandraSpec extends FunSpec
  with Matchers with ScalatestRouteTest with BeforeAndAfterAll
  with BeforeAndAfterEach with MockitoSugar {

  private implicit var session: Session = null
  private var cluster: Cluster = null

  override def beforeAll = {
    System.getProperties.setProperty("ENVIRONMENT", "test")
    EmbeddedCassandraServerHelper.startEmbeddedCassandra(EmbeddedCassandraServerHelper.CASSANDRA_RNDPORT_YML_FILE);
    cluster = CassandraConfig.defaultCluster
    session = cluster.connect()
    setupKeyspace()
  }


  private def setupKeyspace()(implicit session: Session) = {
    session.execute(Source.fromFile("src/test/resources/v2/create_keyspace.cql")
      .mkString.replace("_keyspace", CassandraConfig.keyspace))

    session.execute(Source.fromFile("src/test/resources/v2/create_apps.cql")
      .mkString.replace("_keyspace", CassandraConfig.keyspace))

    session.execute(Source.fromFile("src/test/resources/v2/create_current_version_apps.cql")
      .mkString.replace("_keyspace", CassandraConfig.keyspace))

    session.execute(Source.fromFile("src/test/resources/v2/create_ratings.cql")
      .mkString.replace("_keyspace", CassandraConfig.keyspace))

    Source.fromFile("src/test/resources/v2/insert_current_version_apps.cql").getLines().foreach(l =>
      session.execute(l.replaceAll("_keyspace", CassandraConfig.keyspace))
    )

    Source.fromFile("src/test/resources/v2/insert_apps.cql").getLines().foreach(l =>
      session.execute(l.replaceAll("_keyspace", CassandraConfig.keyspace))
    )

    Source.fromFile("src/test/resources/v2/insert_ratings.cql").getLines().foreach(l =>
      session.execute(l.replaceAll("_keyspace", CassandraConfig.keyspace))
    )

    session.execute("USE " + CassandraConfig.keyspace)
  }

}
