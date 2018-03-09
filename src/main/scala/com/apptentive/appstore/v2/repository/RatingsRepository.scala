package com.apptentive.appstore.v2.repository

import java.time.ZoneOffset
import java.util.Date

import com.apptentive.appstore.v2.config.CassandraConfig
import com.apptentive.appstore.v2.model._
import com.apptentive.appstore.v2.util.DateUtils.{parseDate, toDateTime}
import com.datastax.driver.core.querybuilder.{QueryBuilder, Select}
import com.datastax.driver.core.{Row, Session}

import scala.collection.JavaConverters._

class RatingsRepository extends BaseRepository {
  def getRatingsByDate(store: AppStore, storeId: String, date: String)(implicit session: Session): Option[Ratings] = {
    val startDate = Date.from(toDateTime(parseDate(date)).toLocalDate.atStartOfDay().toInstant(ZoneOffset.UTC))
    val endDate = Date.from(toDateTime(parseDate(date)).toLocalDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))
    val query = QueryBuilder.select()
      .from(CassandraConfig.keyspace, "ratings")
      .where()
      .and(QueryBuilder.eq("store", store.store))
      .and(QueryBuilder.eq("store_id", storeId))
      .and(QueryBuilder.gte("ingest_time", startDate))
      .and(QueryBuilder.lt("ingest_time", endDate))

    val ratings = session.execute(query).all().asScala.map(fromRow).toList

    ratings.headOption
  }

  def getRatings(store: AppStore, storeId: String, paginationParams: PaginationParams, queryParameters: RatingsQueryParameters)(implicit session: Session): QueryResult[Ratings] = {
    val query = QueryBuilder.select()
      .from(CassandraConfig.keyspace, "ratings")
      .where()
      .and(QueryBuilder.eq("store", store.store))
      .and(QueryBuilder.eq("store_id", storeId))

    val queryWithParams = applyQueryParams(queryParameters, query)
    val paginatedQuery = paginate(paginationParams, queryWithParams)
    val orderedQuery = applyOrdering(paginationParams, paginatedQuery)
    val ratings = session.execute(orderedQuery).all().asScala.map(fromRow).toList

    QueryResult.fromItems(ratings, paginationParams)
  }

  private def applyQueryParams(queryParameters: RatingsQueryParameters, baseQuery: Select.Where) = {
    var query = baseQuery

    for (startDate <- queryParameters.startDate;
         endDate <- queryParameters.endDate) {
      query = query.and(QueryBuilder.gte("ingest_time", startDate.toInstant.toEpochMilli))
      val endDateAdjusted = toDateTime(endDate).toLocalDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli
      query = query.and(QueryBuilder.lte("ingest_time", endDateAdjusted))
    }

    query
  }

  private def applyOrdering(paginationParams: PaginationParams, baseQuery: Select) = {
    paginationParams.sortOrder.map {
      case SortOrder.Ascending => baseQuery.orderBy(QueryBuilder.asc("ingest_time"))
      case SortOrder.Descending => baseQuery.orderBy(QueryBuilder.desc("ingest_time"))
      case _ => throw new IllegalArgumentException("unknown sort order")
    }.getOrElse(baseQuery)
  }

  private def paginate(paginationParams: PaginationParams, query: Select.Where) = {
    paginate(paginationParams, query, "ingest_time")
  }

  private def fromRow(row: Row): Ratings = {
    Ratings(
      row.getString("store_id"),
      row.getString("store"),
      row.getString("region"),
      toDateTime(row.getTimestamp("ingest_time")),
      toDateTime(row.getTimestamp("store_observed_time")),
      row.getMap("all_ratings_histogram", classOf[Integer], classOf[java.lang.Long]).asScala.toMap
        .map({ case (i: Integer, l: java.lang.Long) => (i.toInt, l.toLong) }),
      row.getDouble("all_ratings_average"),
      row.getLong("all_ratings_count"),
      row.getMap("curr_ratings_histogram", classOf[Integer], classOf[java.lang.Long]).asScala.toMap
        .map({ case (i: Integer, l: java.lang.Long) => (i.toInt, l.toLong) }),
      row.getDouble("curr_ratings_average"),
      row.getLong("curr_ratings_count"),
      row.getString("version")
    )
  }
}
