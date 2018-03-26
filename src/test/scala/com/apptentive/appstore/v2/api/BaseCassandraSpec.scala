package com.apptentive.appstore.v2.api

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.apptentive.appstore.v2.config.CassandraConfig
import com.apptentive.appstore.v2.util.KeyspaceInitialzier
import com.datastax.driver.core.{Cluster, Session}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSpec, Matchers}

trait BaseCassandraSpec extends FunSpec
  with Matchers with ScalatestRouteTest with BeforeAndAfterAll
  with BeforeAndAfterEach with MockitoSugar {

  private implicit var session: Session = null
  protected var cluster: Cluster = null

  override def beforeAll = {
    EmbeddedCassandraServerHelper.startEmbeddedCassandra(EmbeddedCassandraServerHelper.CASSANDRA_RNDPORT_YML_FILE);
    cluster =
      Cluster.builder()
        .addContactPoint(EmbeddedCassandraServerHelper.getHost)
        .withPort(EmbeddedCassandraServerHelper.getNativeTransportPort)
        .build()

    session = cluster.connect()
    KeyspaceInitialzier.setupKeyspace()
  }


  override def afterAll = {
    session.execute("DROP KEYSPACE " + CassandraConfig.keyspace)
    session.close()
    cluster.close()
  }
}
