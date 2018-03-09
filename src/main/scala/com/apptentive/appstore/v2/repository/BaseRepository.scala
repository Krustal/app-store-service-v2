package com.apptentive.appstore.v2.repository

import java.util.Date

import com.apptentive.appstore.v2.model.{PaginationParams, SortOrder}
import com.datastax.driver.core.querybuilder.{QueryBuilder, Select}

trait BaseRepository {
  def paginate(pagination: PaginationParams, query: Select.Where, field: String) = {

    val sortOrder = pagination.sortOrder.getOrElse("desc")

    if (pagination.minKey.isDefined) {
      val minKey = pagination.minKey.get
      sortOrder match {
        case SortOrder.Ascending => query.and(QueryBuilder.gte(field, new Date(minKey * 1000))).limit(pagination.pageSize + 1)
        case SortOrder.Descending => query.and(QueryBuilder.lte(field, new Date(minKey * 1000))).limit(pagination.pageSize + 1)
        case _ => throw new IllegalArgumentException("unknown sort order")
      }
    }
    else
      query.limit(pagination.pageSize + 1)
  }

}
