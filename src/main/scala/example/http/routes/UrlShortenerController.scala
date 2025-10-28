package example.http.routes

import example.http.models.ShortUrlResponse
import example.json.HttpUnmarshallers._
import example.json.JsonSupport._
import example.shortener.EventShortenerService
import org.apache.pekko.http.scaladsl.model.ContentTypes.`text/html(UTF-8)`
import org.apache.pekko.http.scaladsl.model.headers.Location
import org.apache.pekko.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes, Uri}
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route

import java.net.URL
import scala.concurrent.ExecutionContext.Implicits.global
object UrlShortenerController {

  def apply(shortenerService: EventShortenerService): Route = {
    (path("shorten")
      & post
      & extractHost
      & extractScheme
      & parameter("url".as[URL])) { (host, scheme, longUrl) =>
        complete(
          shortenerService
            .shorten(longUrl)
            .map { urlHash =>
              ShortUrlResponse(new URL(s"$scheme://$host:9000/$urlHash"))
            }
        )
    } ~ path(Segment) { urlHash =>
      val redirectionType = StatusCodes.MovedPermanently
      complete(
        shortenerService
          .lookup(urlHash)
          .map(url => {
            val uri = Uri(url.toString)
            HttpResponse(
              status = redirectionType,
              headers = Location(uri) :: Nil,
              entity = redirectionType.htmlTemplate match {
                case "" => HttpEntity.Empty
                case template => HttpEntity(`text/html(UTF-8)`, template.format(uri))
              })
          }
          ),
      )
    }
  }
}
