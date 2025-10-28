package example.json

import example.http.models.ShortUrlResponse
import io.circe._
import io.circe.generic.semiauto._
import org.mdedetrich.pekko.http.support.CirceHttpSupport

import java.net.URL

object JsonSupport extends CirceHttpSupport {
  implicit val urlEncoder: Encoder[URL] = Encoder.instance(url => Json.fromString(url.toString))
  implicit val shortenedUrlResponseEncoded: Encoder[ShortUrlResponse] = deriveEncoder[ShortUrlResponse]
}
