package example.shortener

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

class UrlShortenerService(repository: ShortUrlRepository, hashGenerator: HashGenerator)(implicit ec: ExecutionContext) {

  //How many times do we retry re-generating a shortUrl in case of collissions
  private val MAX_RETRIES = 10

  private def retry[A](future: => Future[A], remainingAttempts: Int): Future[A] =
    if (remainingAttempts <= 0) future
    else future.recoverWith {
      case _: DuplicateShortenedUrlException => retry(future, remainingAttempts - 1)
      case otherError => Future.failed(otherError)
    }

  final def shorten(longUrl: URL): Future[String] = {
    for {
      urlHash <- hashGenerator.generateHash(longUrl)
      insertedWithoutDuplication <- retry(
        for {
          //On each attempt we attempt to generate a new short URL and try to insert it.
          //We retry if the shortURL is already present.
          //The failure rate will increate when we use up a large portion of the available shortUrls
          //which are currently a 7 character string, so we can come up with a more efficient way of generating
          //shortUrls
          shortUrl <- hashGenerator.generateShortUrl()
          shortUrlInserted <- repository.tryInsert(shortUrl, urlHash, longUrl)
        } yield shortUrlInserted,
        MAX_RETRIES
      )
    } yield insertedWithoutDuplication
  }
}
