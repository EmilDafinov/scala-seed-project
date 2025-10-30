package example.shortener

import org.postgresql.util.PSQLException
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

case class DuplicateShortenedUrlException(message: String, cause: Throwable) extends RuntimeException(message, cause)

class ShortUrlRepository(dbConfig: Database)(implicit ec: ExecutionContext) {

  private val POSTGRES_UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = "23505"

  def tryInsert(shortUrl: String, urlHash: String, longUrl: URL): Future[String] = {

    //Looking up the URL by its hash allows us to take advantage of the index for
    dbConfig.run(
      sql"""
          SELECT short_url
          FROM shortened_urls
          WHERE url_hash = $urlHash
            AND full_url = ${longUrl.toString}
      """.as[String].headOption
      .flatMap {
        case Some(existingShortUrl) => DBIO.successful(existingShortUrl)
        case None => sqlu"""
        INSERT INTO shortened_urls(short_url, url_hash, full_url)
        VALUES ($shortUrl, $urlHash, ${longUrl.toString})
      """.map(_ => shortUrl)
      }
      .transactionally
    ).recoverWith {
      case e: PSQLException if e.getSQLState == POSTGRES_UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE  =>
        Future.failed(DuplicateShortenedUrlException(s"Attempted to insert a shortUrl that is already present: $shortUrl", e))
    }
  }

  def resolve(shortUrl: String): Future[Option[URL]] = {
    dbConfig.run(
      sql"""
          SELECT full_url
          FROM shortened_urls
          WHERE short_url = $shortUrl
      """.as[String]
    )
    .map(_.headOption.map(url => new URL(url)))
  }
}