package com.apptentive.appstore.v2.model

import com.datastax.driver.core.querybuilder.{Ordering, QueryBuilder}

/** Simple class that encapsulates query ordering. */
object SortOrder {
  val Ascending = "asc"
  val Descending = "desc"

  val Default: String = Descending

  /**
    * Normalize user input sort order string values to "asc" for Ascending; "desc" for Descending.
    *
    * @param order String representation of sort order to normalize
    * @return String representation of order as a constant
    * @throws IllegalArgumentException when unable to normalize order value
    */
  def normalize(order: String): String =
    order.toLowerCase() match {
      case s if s.startsWith(Ascending) => Ascending
      case s if s.startsWith(Descending) => Descending
      case _ => throw new IllegalArgumentException("Sort order must be 'asc' or 'desc'")
    }

  /**
    * Create a Ordering Clause for a Cassandra query from the given order/key strings.
    *
    * @param order String representation of sort order
    * @param field String representation of field name to order on
    * @return Ordering clause
    * @throws IllegalArgumentException when unable to normalize order value
    */
  def toClause(order: String, field: String): Ordering =
    SortOrder.normalize(order) match {
      case SortOrder.Ascending => QueryBuilder.asc(field)
      case SortOrder.Descending=> QueryBuilder.desc(field)
    }
}
