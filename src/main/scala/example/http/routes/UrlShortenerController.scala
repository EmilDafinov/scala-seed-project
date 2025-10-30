package example.http.routes

import example.http.models.ShortUrlResponse
import example.json.HttpUnmarshallers._
import example.json.JsonSupport._
import example.shortener.{EventShortenerService, ShortUrlRepository}
import org.apache.pekko.http.scaladsl.model.ContentTypes.`text/html(UTF-8)`
import org.apache.pekko.http.scaladsl.model.HttpEntity.Empty
import org.apache.pekko.http.scaladsl.model.StatusCodes.MovedPermanently
import org.apache.pekko.http.scaladsl.model.headers.Location
import org.apache.pekko.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes, Uri}
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route

import java.net.URL
import scala.concurrent.ExecutionContext.Implicits.global

object UrlShortenerController {

  def apply(shortenerService: EventShortenerService, repository: ShortUrlRepository): Route = {
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
      rejectEmptyResponse {
        complete(
          repository
            .resolveFullUrl(urlHash)
            .map(_.map(toRedirectResponse)),
        )
      }
    }
  }

  private def toRedirectResponse(url: URL) = {
    val redirectionType = MovedPermanently
    val uri = Uri(url.toString)
    HttpResponse(
      status = redirectionType,
      headers = Location(uri) :: Nil,
      entity = redirectionType.htmlTemplate match {
        case "" => Empty
        case template => HttpEntity(`text/html(UTF-8)`, template.format(uri))
      })
  }
}
