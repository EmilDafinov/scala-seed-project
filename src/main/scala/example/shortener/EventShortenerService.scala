package example.shortener

import java.net.URL
import java.util.zip.CRC32C
import scala.concurrent.{ExecutionContext, Future}

class EventShortenerService(implicit ec: ExecutionContext) {

  def shorten(longUrl: URL): Future[String] = Future {
    val crc = new CRC32C()
    crc.update(longUrl.toExternalForm.getBytes)
    crc.getValue.toHexString
  }


  def lookup(urlHash: String): Future[URL] = Future {
    new URL("https://medium.com/streamingdata/running-kafka-locally-inside-kubernetes-25e84586bbf3")
  }

}
