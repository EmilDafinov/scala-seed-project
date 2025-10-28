package example.events

import com.typesafe.config.{Config, ConfigFactory}
import example.AkkaDependenciesModule
import slick.jdbc.JdbcBackend.Database

trait EventsModule {
  this: AkkaDependenciesModule =>

  lazy val db = Database.forConfig(
    path = "postgres.db"
  )
  private lazy val config: Config = ConfigFactory.load()

  private lazy val eventRepository: EventRepository = new EventRepository(db)




}
