package example.shortener

import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._

import java.net.URL
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UrlShortenerServiceTest extends UnitTestSpec {

  private val mockHashGenerator: HashGenerator = mock[HashGenerator]
  private val mockRepository: ShortUrlRepository = mock[ShortUrlRepository]
  private val mockJedis = mock[RedisUrlResolver]

  val tested = new UrlShortenerService(mockRepository, mockJedis, mockHashGenerator)

  override def beforeEach(): Unit = reset(mockHashGenerator, mockRepository, mockJedis)

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

    "not retry on errors different from a duplicate insert" in {
      Given("a URL")
      val url = new URL("https://medium.com/streamingdata/running-kafka-locally-inside-kubernetes-25e84586bbf3")

      And("a test hash for the URL")

      val testUrlHash = "abcdefgh"
      when {
        mockHashGenerator.generateHash(url)
      } thenReturn {
        Future.successful(testUrlHash)
      }

      And("a test shortUrl code for the url")
      val shortUrlAlreadyPresentInDb = "testShortUrl"
      when {
        mockHashGenerator.generateShortUrl()
      } thenReturn {
        Future.successful(shortUrlAlreadyPresentInDb)
      }

      And("the repository throws an error different from a duplicate key")
      val testErrorMessage = "Kaboom"
      when {
        mockRepository.tryInsert(shortUrlAlreadyPresentInDb, testUrlHash, url)
      } thenReturn {
        Future.failed(new RuntimeException(testErrorMessage))
      }


      When("we attempt to shorten our test url")
      val eventualResult = tested.shorten(url)

      Then("the operation should fail without retrying")
      whenReady(eventualResult.failed) { actualError =>
        actualError.getMessage shouldEqual testErrorMessage
        verify(mockRepository, times(1)).tryInsert(anyString(), anyString(), any[URL])
      }
    }

    "not resolve a URL from a shortCode if the URL not present in cache nor db" in {
      Given("a shortUrl code")
      val testShortUrl = "abcd"
      And("the test shortUrl is not present in the cache")
      when {
        mockJedis.resolve(testShortUrl)
      } thenReturn {
        Future.successful(None)
      }

      And("the test shortUrl is not present in the database")
      when {
        mockRepository.resolve(testShortUrl)
      } thenReturn {
        Future.successful(None)
      }

      When("we attempt resolving a URL from a shortUrl code")
      val eventualResult = tested.resolve(testShortUrl)

      Then("we should get an empty result")
      whenReady(eventualResult) { maybeActualResult =>
        maybeActualResult shouldBe empty
      }
    }

    "resolve a URL from cache if the shortCode is a valid key" in {
      Given("a shortUrl code")
      val testShortUrl = "abcd"
      val cachedLongUrl = new URL("http://www.example.com")
      And("the test shortUrl is a valid key in the cache")
      when {
        mockJedis.resolve(testShortUrl)
      } thenReturn {
        Future.successful(Some(cachedLongUrl))
      }

      When("we attempt resolving a URL from a shortUrl code")
      val eventualResult = tested.resolve(testShortUrl)

      whenReady(eventualResult) { maybeActualResult =>
        Then("we should get the cached URL")
        maybeActualResult should contain (cachedLongUrl)
        And("the database should never be invoked")
        verify(mockRepository, never()).resolve(anyString)
      }
    }

    "resolve a URL from a shortCode if it is absent from the cache, but present in the db" in {
      Given("a shortUrl code")
      val testShortUrl = "abcd"
      val longUrlStoredInDb = new URL("http://www.example.com")

      And("the test shortUrl is not present in the cache")
      when {
        mockJedis.resolve(testShortUrl)
      } thenReturn {
        Future.successful(None)
      }

      And("the test shortUrl is present in the database")
      when {
        mockRepository.resolve(testShortUrl)
      } thenReturn {
        Future.successful(Some(longUrlStoredInDb))
      }

      When("we attempt resolving a URL from a shortUrl code")
      val eventualResult = tested.resolve(testShortUrl)

      Then("we should get the result from the database")
      whenReady(eventualResult) { maybeActualResult =>
        maybeActualResult should contain (longUrlStoredInDb)
      }
    }
  }
}
