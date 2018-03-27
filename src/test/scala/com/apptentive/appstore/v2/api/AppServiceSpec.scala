package com.apptentive.appstore.v2.api

import akka.http.scaladsl.model._
import com.apptentive.appstore.v2.repository.AppRepository
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, JNothing, JObject, NoTypeHints}

class AppServiceSpec extends BaseCassandraSpec {
  private var appService: AppService = null

  private implicit val formats = org.json4s.DefaultFormats

  override def beforeAll() = {
    super.beforeAll()
    appService = new AppService(new AppRepository, cluster)
  }

  describe("Health") {
    it("should respond") {
      val getReq = HttpRequest(
        HttpMethods.GET,
        uri = s"/api/v2/health")

      getReq ~> appService.routes ~> check({
        handled shouldBe true
        status.isSuccess() shouldBe true
        val result = entityAs[String]
        result shouldBe "OK"
      })
    }
  }

  describe("App API") {
    describe("Apps Queries") {
      describe ("retrieving apps") {
        it("should get current app version") {
          val storeAppId = "com.secretwhisper.bibleverses"

          val getReq = HttpRequest(
            HttpMethods.GET,
            uri = s"/api/v2/store/android/apps/${storeAppId}")

          getReq ~> appService.routes ~> check({
            handled shouldBe true
            status.isSuccess() shouldBe true
            contentType shouldBe ContentTypes.`application/json`
            val result = parse(entityAs[String])
            (result \ "store_id").extract[String] shouldBe "com.secretwhisper.bibleverses"
            (result \ "version").extract[String] shouldBe "tbd2"
          })
        }

        it("should 404 if unknown app") {
          val storeAppId = "com.secretwhisper.bibleverses1"

          val getReq = HttpRequest(
            HttpMethods.GET,
            uri = s"/api/v2/store/android/apps/${storeAppId}")

          getReq ~> appService.routes ~> check({
            handled shouldBe true
            status.intValue() shouldBe 404
          })
        }

        it("should get apps by store") {
          val getReq = HttpRequest(
            HttpMethods.GET,
            uri = s"/api/v2/store/android/apps")

          getReq ~> appService.routes ~> check({
            handled shouldBe true
            status.isSuccess() shouldBe true
            contentType shouldBe ContentTypes.`application/json`
            val result = parse(entityAs[String])
            (result \ "data").values.asInstanceOf[Seq[JObject]].length shouldBe 50
            ((result \ "data") (0) \ "store_id").extract[String] shouldBe "com.secretwhisper.bibleverses"
            (result \ "page_size").extract[Int] shouldBe 50
            (result \ "has_more").extract[Boolean] shouldBe true
          })
        }
      }

      describe("pagination") {
        it("should paginate with default options") {
          implicit val formats = DefaultFormats
          val storeAppId = "com.secretwhisper.bibleverses"
          val defaultPageSize = 50
          val baseUri = "/api/v2/store/android/apps/" + storeAppId + "/versions"

          val getReq = HttpRequest(HttpMethods.GET, uri = baseUri)

          getReq ~> appService.routes ~> check({
            status.isSuccess() shouldBe true
            contentType shouldBe ContentTypes.`application/json`
            val result = parse(entityAs[String])
            (result \ "data").values.asInstanceOf[Seq[JObject]].length shouldBe defaultPageSize
            (result \ "page_size").extract[Int] shouldBe defaultPageSize
            (result \ "has_more").extract[Boolean] shouldBe true
            (result \ "min_key") shouldNot be(JNothing)
          })
        }

        it("should paginate properly") {
          implicit val formats = DefaultFormats
          val storeAppId = "com.secretwhisper.bibleverses"
          val pageSize = 1
          val baseUri = s"/api/v2/store/android/apps/$storeAppId/versions?page_size=$pageSize"
          val expectedMinKey = 1520646542000L

          val getReq = HttpRequest(HttpMethods.GET, uri = baseUri)

          getReq ~> appService.routes ~> check({
            status.isSuccess() shouldBe true
            contentType shouldBe ContentTypes.`application/json`
            val result = parse(entityAs[String])
            (result \ "data").values.asInstanceOf[Seq[JObject]].length shouldBe pageSize
            (result \ "page_size").extract[Int] shouldBe pageSize
            (result \ "has_more").extract[Boolean] shouldBe true
            (result \ "min_key").extract[Long] should be(expectedMinKey)
          })

          val pagedUri = s"/api/v2/store/android/apps/$storeAppId/versions?page_size=$pageSize&min_key=$expectedMinKey"
          val pagedReq = HttpRequest(HttpMethods.GET, uri = pagedUri)

          pagedReq ~> appService.routes ~> check({
            status.isSuccess() shouldBe true
            contentType shouldBe ContentTypes.`application/json`
            val result = parse(entityAs[String])
            (result \ "data").values.asInstanceOf[Seq[JObject]].length shouldBe pageSize
            (result \ "page_size").extract[Int] shouldBe pageSize
            (result \ "has_more").extract[Boolean] shouldBe true
            (result \ "min_key").extract[Long] should be(1520646542000L)
          })
        }
      }

      describe("app version filtering by date") {
        it("should get correct app version for GET to /api/v2/apps/?as-of") {
          val storeId = "com.secretwhisper.bibleverses"
          val asOf = "2018-03-05"

          val getReq = HttpRequest(
            HttpMethods.GET,
            uri = "/api/v2/store/android/apps/" + storeId + "/versions?as-of=" + asOf)

          getReq ~> appService.routes ~> check({
            status.isSuccess() shouldBe true
            val result = parse(entityAs[String])
            contentType shouldBe ContentTypes.`application/json`
            (result \ "data").values.asInstanceOf[Seq[JObject]].length shouldBe 45
            ((result \ "data") (0) \ "store_id").extract[String] shouldBe "com.secretwhisper.bibleverses"
            ((result \ "data") (0) \ "version").extract[String] shouldBe "tbd45"
          })
        }
      }

      describe("show individual app version") {
        it("should fetch singular apps") {
          implicit val formats = Serialization.formats(NoTypeHints)

          val storeId = "com.secretwhisper.bibleverses"
          val version = "tbd11"

          val getReq = HttpRequest(
            HttpMethods.GET,
            uri = "/api/v2/store/android/apps/" + storeId + "/versions/" + version)

          getReq ~> appService.routes ~> check({
            status.isSuccess() shouldBe true
            contentType shouldBe ContentTypes.`application/json`
            (parse(entityAs[String]) \ "version").extract[String] shouldBe version
          })
        }

        it("should fetch current version") {
          implicit val formats = Serialization.formats(NoTypeHints)

          val storeId = "com.secretwhisper.bibleverses"

          val getReq = HttpRequest(
            HttpMethods.GET,
            uri = "/api/v2/store/android/apps/" + storeId + "/versions/current")

          getReq ~> appService.routes ~> check({
            status.isSuccess() shouldBe true
            contentType shouldBe ContentTypes.`application/json`
            (parse(entityAs[String]) \ "version").extract[String] shouldBe "tbd2"
          })
        }

        it("should not fetch missing version") {
          implicit val formats = Serialization.formats(NoTypeHints)

          val storeId = "com.secretwhisper.bibleverses"

          val getReq = HttpRequest(
            HttpMethods.GET,
            uri = "/api/v2/store/android/apps/" + storeId + "/versions/doesnotexist")

          getReq ~> appService.routes ~> check({
            status.isSuccess() shouldBe true
            contentType shouldBe ContentTypes.`application/json`
            entityAs[String] == "{}"
          })
        }
      }
    }
  }
}
