package com.apptentive.appstore.v2.repository

import com.apptentive.appstore.v2.config.CassandraConfig
import com.apptentive.appstore.v2.model._
import com.apptentive.appstore.v2.util.DateUtils
import com.datastax.driver.core.querybuilder.{QueryBuilder, Select}
import com.datastax.driver.core.{Row, Session}

import scala.collection.JavaConverters._

class AppRepository extends BaseRepository {
  def findByStore(store: AppStore, pagination: PaginationParams, queryParameters: AppQueryParameters)(implicit session: Session): QueryResult[App] = {
    val query = QueryBuilder.select()
      .from(CassandraConfig.keyspace, "current_version_apps")
      .where(QueryBuilder.eq("store", store.store))

    val apps = session.execute(paginate(pagination, applyQueryParams(queryParameters, query)).allowFiltering()).all().asScala.map(fromRow).toList

    QueryResult.fromItems(apps, pagination)
  }

  def findVersions(store: AppStore, storeId: String, pagination: PaginationParams, queryParameters: AppQueryParameters)(implicit session: Session): QueryResult[App] = {
    val query = QueryBuilder.select().from(CassandraConfig.keyspace, "apps").where()
      .and(QueryBuilder.eq("store_id", storeId))

    val queryWithParams = applyQueryParams(queryParameters, query)
    val paginatedQuery = paginate(pagination, queryWithParams)
    val apps = session.execute(paginatedQuery).all().asScala.map(fromRow).toList

    QueryResult.fromItems(apps, pagination)
  }

  def findVersion(store: AppStore, storeId: String, version: String)(implicit session: Session): Option[App] = {
    val table = if (version == "current") "current_version_apps" else "apps"

    val query = QueryBuilder.select()
      .from(CassandraConfig.keyspace, table).where(QueryBuilder.eq("store_id", storeId))

    val appVersions = session.execute(query).all().asScala.map(fromRow).toList

    if (appVersions.length == 1)
      Some(appVersions.head)
    else
      appVersions.find(_.version == version)
  }

  private def applyQueryParams(queryParameters: AppQueryParameters, baseQuery: Select.Where) = {
    queryParameters.asOf.map(asOf => baseQuery.and(QueryBuilder.lte("updated_date", asOf))).getOrElse(baseQuery)
  }

  private def paginate(pagination: PaginationParams, query: Select.Where): Select = {
    paginate(pagination, query, "updated_date")
  }

  private def fromRow(row: Row): App = {
    App(
      row.getString("store_id"),
      row.getString("store"),
      row.getString("category"),
      row.getString("developer"),
      row.getString("title"),
      row.getString("icon"),
      row.getString("version"),
      row.getTimestamp("updated_date").getTime,
      row.getTimestamp("ingestion_time").getTime
    )
  }
}
