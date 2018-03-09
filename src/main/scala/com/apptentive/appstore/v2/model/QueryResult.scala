package com.apptentive.appstore.v2.model


object QueryResult {
  def fromItems[T <: Pageable](items: Seq[T], paginationParams: PaginationParams): QueryResult[T] = {
    if (items.isEmpty) {
      QueryResult(items, paginationParams.pageSize, false, 0)
    }
    else {
      val last = items.last
      val hasMore = items.size > paginationParams.pageSize
      val minKey: Long = if (hasMore) last.nextMinKey else 0
      val displayedApps = if (hasMore) items.dropRight(1) else items
      QueryResult(displayedApps, paginationParams.pageSize, hasMore, minKey)
    }
  }
}

case class QueryResult[T <: Pageable](
  data: Seq[T],
  pageSize: Int,
  hasMore: Boolean,
  minKey: Long
)