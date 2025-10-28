package example.http.routes

import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import example.json.HttpUnmarshallers._

import java.net.URL
object UrlShortenerController {

  def apply(): Route = {
    (path("shorten")
      & post
      & parameter("url".as[URL])) { abv =>
        complete(abv.toString)
    }
  }
}
