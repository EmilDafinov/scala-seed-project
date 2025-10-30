package example.shortener


import java.net.URL
import java.util.zip.CRC32
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

trait UrlHashGenerator {
  def generateHash(url: URL): Future[String]
}

trait ShortUrlGenerator {
  def generateShortUrl(): Future[String]
}

class HashGenerator(random: Random)(implicit ec: ExecutionContext) extends UrlHashGenerator with ShortUrlGenerator{

  def generateHash(url: URL): Future[String] = Future {
    val crc = new CRC32
    crc.update(url.toString.getBytes)
    crc.getValue.toHexString
  }

  def generateShortUrl(): Future[String] = Future {
    random.alphanumeric.take(7).mkString
  }
}
