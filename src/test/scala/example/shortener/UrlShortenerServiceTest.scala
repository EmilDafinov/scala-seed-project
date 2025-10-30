package example.shortener

import org.mockito.Mockito.when

import java.net.URL
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UrlShortenerServiceTest extends UnitTestSpec {

  private val mockHashGenerator: HashGenerator = mock[HashGenerator]
  private val mockRepository: ShortUrlRepository = mock[ShortUrlRepository]

  val tested = new UrlShortenerService(mockRepository, mockHashGenerator)

  "EventShortenerService" should {

    "generate a new hash and short URL for a new URL and persist them" in {
      Given("a URL that doesn't exist in our database")
      val url = new URL("https://medium.com/streamingdata/running-kafka-locally-inside-kubernetes-25e84586bbf3")

      And("a test hash for the URL")

      val testUrlHash = "abcdefgh"
      when {
        mockHashGenerator.generateHash(url)
      } thenReturn {
        Future.successful(testUrlHash)
      }

      And("a test shortUrl code that is not present in the db")

      val testShortUrl = "testShortUrl"
      when {
        mockHashGenerator.generateShortUrl()
      } thenReturn {
        Future.successful(testShortUrl)
      }

      And("the test shortUrl is not present in the db")
      when {
        mockRepository.tryInsert(testShortUrl, testUrlHash, url)
      } thenReturn {
        Future.successful(testShortUrl)
      }

      When("we attempt to shorten our test url")
      val eventualResult = tested.shorten(url)

      Then("the test shorUrl that was generate is successfully stored and returned")
      whenReady(eventualResult) { actualShortUrl =>
        actualShortUrl shouldEqual testShortUrl
      }
    }

    "retry the insert with a different short url in case the first one already exists" in {
      Given("a URL that doesn't exist in our database")
      val url = new URL("https://medium.com/streamingdata/running-kafka-locally-inside-kubernetes-25e84586bbf3")

      And("a test hash for the URL")

      val testUrlHash = "abcdefgh"
      when {
        mockHashGenerator.generateHash(url)
      } thenReturn {
        Future.successful(testUrlHash)
      }

      And("a test shortUrl code that is already present in the db")

      val shortUrlAlreadyPresentInDb = "testShortUrl"
      when {
        mockHashGenerator.generateShortUrl()
      } thenReturn {
        Future.successful(shortUrlAlreadyPresentInDb)
      }

      val shortUrlNotPresentInDb = "testShortUrl2"
      when {
        mockHashGenerator.generateShortUrl()
      } thenReturn {
        Future.successful(shortUrlNotPresentInDb)
      }

      And("the first shortUrl generated is already present in the db")
      when {
        mockRepository.tryInsert(shortUrlAlreadyPresentInDb, testUrlHash, url)
      } thenReturn {
        Future.failed(DuplicateShortenedUrlException("Short URL already exists", new RuntimeException()))
      }

      And("the second shortUrl generated is NOT present in the db")
      when {
        mockRepository.tryInsert(shortUrlNotPresentInDb, testUrlHash, url)
      } thenReturn {
        Future.successful(shortUrlNotPresentInDb)
      }

      When("we attempt to shorten our test url")
      val eventualResult = tested.shorten(url)

      Then("the test shorUrl that was generate is successfully stored and returned")
      whenReady(eventualResult) { actualShortUrl =>
        actualShortUrl shouldEqual shortUrlNotPresentInDb
      }
    }
  }
}
