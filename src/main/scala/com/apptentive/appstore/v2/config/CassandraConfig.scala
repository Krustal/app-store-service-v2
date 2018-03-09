package com.apptentive.appstore.v2.config

import java.net.InetAddress

import com.datastax.driver.core.{Cluster, Session}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper

import scala.collection.JavaConversions._


object CassandraConfig {

  def propOrEnvOrElse(key: String, default: String): String =
    sys.env.get(key) match {
      case None => sys.props.getOrElse(key, default)
      case Some(value: String) => value
    }

  def host: Seq[InetAddress] = {
    propOrEnvOrElse("ENVIRONMENT", "") match {
      case "test" => Seq(EmbeddedCassandraServerHelper.getHost).map(InetAddress.getByName)
      case default => propOrEnvOrElse("CASSANDRA_HOSTS", "") match {
        case "" => Seq("localhost").map(InetAddress.getByName)
        case other => other.split(",").map(InetAddress.getByName)
      }
    }
  }

  def port: Int = {
    propOrEnvOrElse("ENVIRONMENT", "") match {
      case "test" => EmbeddedCassandraServerHelper.getNativeTransportPort
      case default => propOrEnvOrElse("CASSANDRA_PORT", "") match {
        case "" => 9042
        case other => other.toInt
      }
    }
  }

  def keyspace: String = {
    propOrEnvOrElse("CASSANDRA_KEYSPACE", "") match {
      case "" => "app_store_v2_dev"
      case other => other
    }
  }

  def defaultCluster: Cluster =
    Cluster.builder()
      .addContactPoints(host)
      .withPort(port)
      .build()

  def withDefaultCluster(fn: (Cluster, Session) => Unit) = {
    val cluster = CassandraConfig.defaultCluster
    val session = cluster.connect()
    fn(cluster, session)
    session.close()
    cluster.close()
  }
}


