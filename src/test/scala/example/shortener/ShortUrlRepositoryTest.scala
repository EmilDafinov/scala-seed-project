package example.shortener

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._

import java.net.URL
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class ShortUrlRepositoryTest extends UnitTestSpec {

  private lazy val dbConfig = Database.forConfig("postgres.db")
  val tested = new ShortUrlRepository(dbConfig)

  override def beforeEach(): Unit = Await.result(
    dbConfig.run(sqlu"DELETE FROM shortened_urls"),
    Duration.Inf
  )

  override def afterAll(): Unit = dbConfig.close()

  "ShortUrlRepository" should {

    "allow storing a full URL and looking it up by its shortUrl code" in {
      Given("a URLs that is not present in our db")
      val testUrl = new URL("https://medium.com/streamingdata/running-kafka-locally-inside-kubernetes-25e84586bbf3")
      val testUrlHash = "uMmqfaO"
      val shortUrlAtFirstPersistenceAttempt = "abcd"

      When("we attempt shorten the URL and look it up by the shortUrl code")
      val eventualResult = for {
        shortUrl <- tested.tryInsert(shortUrlAtFirstPersistenceAttempt, testUrlHash, testUrl)
        urlReadBack <- tested.resolve(shortUrl)
      } yield urlReadBack

      Then("the initial URL should be read back")
      whenReady(eventualResult) { maybeActualUrl =>
        maybeActualUrl contains testUrl
      }
    }

    "return the same short url code for an existing url" in {

      Given("a URL and its hash value")
      val url = new URL("https://medium.com/streamingdata/running-kafka-locally-inside-kubernetes-25e84586bbf3")
      val testUrlHash = "uMmqfaO"

      When("we attempt to insert it twice with different shortUrs")
      val shortUrlAtFirstPersistenceAttempt = "abcd"
      val shortUrlAtSecontPersistenceAttempt = "dcba"
      val eventualResult = for {
        shortUrlAfterFirstInsert <- tested.tryInsert(shortUrlAtFirstPersistenceAttempt, testUrlHash, url)
        shortUrlAfterSecondInsert <- tested.tryInsert(shortUrlAtSecontPersistenceAttempt, testUrlHash, url)
        rowCount <- numberOfOccurrencesInDbOf(url)
      } yield (shortUrlAfterFirstInsert, shortUrlAfterSecondInsert, rowCount)

      Then("the original short URL is always returned and no duplicate records are found in the db")
      whenReady(eventualResult) { case (firstShortUrl, secondShortUrl, rowCount) =>
        firstShortUrl shouldEqual shortUrlAtFirstPersistenceAttempt
        secondShortUrl shouldEqual firstShortUrl
        rowCount shouldEqual 1
      }
    }

    "be able to insert multiple URLs that share the same hash" in {

      Given("a pair of URLs that have the same hash value")
      val url1 = new URL("https://medium.com/streamingdata/running-kafka-locally-inside-kubernetes-25e84586bbf3")
      val url2 = new URL("https://example.com")
      val testUrlHash = "uMmqfaO"
      val shortUrlAtFirstPersistenceAttempt = "abcd"
      val shortUrlAtSecontPersistenceAttempt = "dcba"

      When("we attempt to insert both URLs")

      val eventualResult = for {
        shortUrlAfterFirstInsert <- tested.tryInsert(shortUrlAtFirstPersistenceAttempt, testUrlHash, url1)
        shortUrlAfterSecondInsert <- tested.tryInsert(shortUrlAtSecontPersistenceAttempt, testUrlHash, url2)
        numberOfOccurrencesOfUrl1 <- numberOfOccurrencesInDbOf(url1)
        numberOfOccurrencesOfUrl2 <- numberOfOccurrencesInDbOf(url2)
      } yield (shortUrlAfterFirstInsert, shortUrlAfterSecondInsert, numberOfOccurrencesOfUrl1, numberOfOccurrencesOfUrl2)

      Then("the returned short urls are distinct")
      whenReady(eventualResult) { case (firstShortUrl, secondShortUrl, numberOfOccurencesOfUrl1, numberOfOccurencesOfUrl2) =>
        secondShortUrl should not equal firstShortUrl
        numberOfOccurencesOfUrl1 shouldEqual 1
        numberOfOccurencesOfUrl2 shouldEqual 1
      }
    }

    "return an error when we attempt to persist a shortUrl that already exists" in {

      Given("a pair of URLs and a single shortUrl code")
      val url1 = new URL("https://medium.com/streamingdata/running-kafka-locally-inside-kubernetes-25e84586bbf3")
      val url2 = new URL("https://example.com")
      val testUrl1Hash = "uMmqfaO"
      val testUrl2Hash = "faOuMmq"
      val duplicatedShortUrl = "abcd"

      When("we attempt to insert both URLs with the same shortUrl code")
      val eventualResult = for {
        _ <- tested.tryInsert(
          shortUrl = duplicatedShortUrl,
          urlHash = testUrl1Hash,
          longUrl = url1
        )
        _ <- tested.tryInsert(
          shortUrl = duplicatedShortUrl,
          urlHash = testUrl2Hash,
          longUrl = url2
        )

      } yield ()

      Then("the method should return a duplicate key error")
      whenReady(eventualResult.failed) { error =>
        error shouldBe a[DuplicateShortenedUrlException]
      }
    }
  }

  private def numberOfOccurrencesInDbOf(url: URL) = {
    dbConfig.run(
      sql"""
        SELECT count(*)
        FROM shortened_urls
        WHERE full_url = ${url.toString}
      """.as[Int].head
    )
  }
}
