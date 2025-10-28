package example.http.routes

import example.AkkaDependenciesModule
import example.events.EventsModule
import example.shortener.EventShortenerService
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route

trait HttpControllersModule {
  this: AkkaDependenciesModule
    with EventsModule =>

  private val service = new EventShortenerService
  val routes: Route =
    HealthController() ~ UrlShortenerController(service)
}
