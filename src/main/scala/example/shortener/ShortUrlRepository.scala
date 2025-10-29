package example.shortener

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

class ShortUrlRepository(dbConfig: Database)(implicit ec: ExecutionContext) {

  def tryInsert(shortUrl: String, longUrl: URL): Future[String] = {
    val abcd =  sql"""
          SELECT full_url
          FROM shortened_urls
          WHERE url_hash = $shortUrl
      """.as[String].map(_.headOption)
      .flatMap {
        case Some(fullUrl) => if (fullUrl == longUrl.toString) DBIO.successful(shortUrl)
                              else DBIO.failed(new RuntimeException("Retry"))
        case None => sqlu"""
          INSERT INTO shortened_urls(url_hash, full_url)
          VALUES (${shortUrl}, ${longUrl.toString})
        """.map(_ => shortUrl)
      }
    dbConfig.run(abcd.transactionally)
  }
//    dbConfig.run(
//      sqlu"""
//        INSERT INTO shortened_urls(url_hash, full_url)
//        VALUES (${shortUrl}, ${longUrl.toString})
//      """
//    ).map(_ => true)

//  Future(false)

  def resolveFullUrl(shortUrl: String): Future[Option[URL]] = {
    dbConfig.run(
      sql"""
          SELECT full_url
          FROM shortened_urls
          WHERE url_hash = $shortUrl
      """.as[String]
    )
    .map(_.headOption.map(url => new URL(url)))
  }
}
