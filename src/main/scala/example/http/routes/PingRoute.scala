package example.http.routes

import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route

object PingRoute {
  def apply(): Route = {
    (path("ping")
      & get) {
      scribe.info("Pinged")
      complete("Ping")
    }
  }
}
