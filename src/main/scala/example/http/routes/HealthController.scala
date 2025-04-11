package example.http.routes

import org.apache.pekko.http.scaladsl.model.HttpResponse
import org.apache.pekko.http.scaladsl.model.StatusCodes.OK
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route

object HealthController {

  private val pingResponse = HttpResponse(status = OK)

  def apply(): Route = {
    (path("health")
      & get) {
      complete(pingResponse)
    }
  }
}
