package example.http.routes

import example.AkkaDependenciesModule
import example.shortener.{EventShortenerService, HashGenerator, ShortUrlRepository}
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import slick.jdbc.JdbcBackend.Database

import scala.util.Random

trait HttpControllersModule {
  this: AkkaDependenciesModule =>

  lazy val db = Database.forConfig(
    path = "postgres.db"
  )

  private val repository = new ShortUrlRepository(db)

  private val service = new EventShortenerService(repository, new HashGenerator(new Random()))

  val routes: Route =
    HealthController() ~ UrlShortenerController(service)
}
