package com.apptentive.appstore.v2.api

import akka.http.scaladsl.model._
import com.apptentive.appstore.v2.repository.RatingsRepository
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, JObject}

class RatingsServiceSpec extends BaseCassandraSpec {
  private var ratingsService: RatingsService = null

  private implicit val formats = org.json4s.DefaultFormats

  override def beforeAll() = {
    super.beforeAll()
    ratingsService = new RatingsService(new RatingsRepository, cluster)
  }

  describe("Review Histograms API") {
    it("should allow GET of ratings histograms") {
      val storeAppId = "1052194777"
      val store = "itunes"

      val getReq = HttpRequest(
        HttpMethods.GET,
        uri = s"/api/v2/store/$store/apps/$storeAppId/ratings-histograms")

      getReq ~> ratingsService.routes ~> check({
        handled shouldBe true
        contentType shouldBe ContentTypes.`application/json`
        status.isSuccess() shouldBe true
      })
    }

    it("should allow GET of ratings histograms on a specific date") {
      val storeAppId = "1052194777"
      val store = "itunes"
      val date = "2018-03-07"

      val getReq = HttpRequest(
        HttpMethods.GET,
        uri = s"/api/v2/store/$store/apps/$storeAppId/ratings-histograms/$date")

      getReq ~> ratingsService.routes ~> check({
        handled shouldBe true
        contentType shouldBe ContentTypes.`application/json`
        status.isSuccess() shouldBe true
        val result = parse(entityAs[String])
        (result \ "version").extract[String] shouldBe "4.4"
      })
    }

    it("should return 404 for a missing date") {
      val storeAppId = "1052194777"
      val store = "itunes"
      val date = "2019-03-07"

      val getReq = HttpRequest(
        HttpMethods.GET,
        uri = s"/api/v2/store/$store/apps/$storeAppId/ratings-histograms/$date")

      getReq ~> ratingsService.routes ~> check({
        contentType shouldBe ContentTypes.`application/json`
        status.intValue() shouldBe 404
      })
    }

    it("it should have inclusive start and end date") {
      implicit val formats = DefaultFormats
      val storeAppId = "1120219625"
      val pageSize = 10
      val startDate = "2018-03-09"
      val endDate = "2018-03-10"
      val baseUri = s"/api/v2/store/itunes/apps/$storeAppId/ratings-histograms?page_size=$pageSize&start_date=$startDate&end_date=$endDate"

      val getReq = HttpRequest(HttpMethods.GET, uri = baseUri)

      getReq ~> ratingsService.routes ~> check({
        status.isSuccess() shouldBe true
        contentType shouldBe ContentTypes.`application/json`
        val result = parse(entityAs[String])
        (result \ "data").values.asInstanceOf[Seq[JObject]].length shouldBe 2
        ((result \ "data") (0) \ "ingest_time").extract[Long] shouldBe 1520705165000L
        ((result \ "data") (1) \ "ingest_time").extract[Long] shouldBe 1520619346000L
        (result \ "page_size").extract[Int] shouldBe pageSize
        (result \ "has_more").extract[Boolean] shouldBe false
      })
    }

    it("it should be able to sort in ascending order") {
      implicit val formats = DefaultFormats
      val storeAppId = "1120219625"
      val pageSize = 10
      val startDate = "2018-03-09"
      val endDate = "2018-03-10"
      val sortOrder = "asc"
      val baseUri = s"/api/v2/store/itunes/apps/$storeAppId/ratings-histograms?page_size=$pageSize&start_date=$startDate&end_date=$endDate&sort_order=$sortOrder"

      val getReq = HttpRequest(HttpMethods.GET, uri = baseUri)

      getReq ~> ratingsService.routes ~> check({
        status.isSuccess() shouldBe true
        contentType shouldBe ContentTypes.`application/json`
        val result = parse(entityAs[String])
        ((result \ "data") (1) \ "ingest_time").extract[Long] shouldBe 1520705165000L
      })
    }

    it("it should be able to sort in descending order") {
      implicit val formats = DefaultFormats
      val storeAppId = "1120219625"
      val pageSize = 10
      val startDate = "2018-03-09"
      val endDate = "2018-03-10"

      val sortOrder = "desc"
      val baseUri = s"/api/v2/store/itunes/apps/$storeAppId/ratings-histograms?page_size=$pageSize&start_date=$startDate&end_date=$endDate&sort_order=$sortOrder"

      val getReq = HttpRequest(HttpMethods.GET, uri = baseUri)

      getReq ~> ratingsService.routes ~> check({
        status.isSuccess() shouldBe true
        contentType shouldBe ContentTypes.`application/json`
        val result = parse(entityAs[String])
        ((result \ "data") (1) \ "ingest_time").extract[Long] shouldBe 1520619346000L
      })
    }
  }

  describe("pagination") {
    it("should paginate properly") {
      implicit val formats = DefaultFormats

      val expectedMinKey = 1520619346000L
      val storeAppId = "1120219625"
      val pageSize = 1
      val baseUri = s"/api/v2/store/itunes/apps/$storeAppId/ratings-histograms?page_size=$pageSize"

      val getReq = HttpRequest(HttpMethods.GET, uri = baseUri)

      getReq ~> ratingsService.routes ~> check({
        status.isSuccess() shouldBe true
        contentType shouldBe ContentTypes.`application/json`
        val result = parse(entityAs[String])
        (result \ "data").values.asInstanceOf[Seq[JObject]].length shouldBe pageSize
        ((result \ "data") (0) \ "ingest_time").extract[Long] shouldBe 1520705165000L
        (result \ "page_size").extract[Int] shouldBe pageSize
        (result \ "has_more").extract[Boolean] shouldBe true
        (result \ "min_key").extract[Long] should be(expectedMinKey)
      })

      val pagedUri = s"/api/v2/store/itunes/apps/$storeAppId/ratings-histograms?page_size=$pageSize&min_key=$expectedMinKey"
      val pagedReq = HttpRequest(HttpMethods.GET, uri = pagedUri)

      pagedReq ~> ratingsService.routes ~> check({
        status.isSuccess() shouldBe true
        contentType shouldBe ContentTypes.`application/json`
        val result = parse(entityAs[String])
        (result \ "data").values.asInstanceOf[Seq[JObject]].length shouldBe pageSize
        ((result \ "data") (0) \ "ingest_time").extract[Long] shouldBe 1520705165000L
        (result \ "page_size").extract[Int] shouldBe pageSize
        (result \ "has_more").extract[Boolean] shouldBe true
        (result \ "min_key").extract[Long] shouldBe 1520619346000L
      })
    }
  }
}
