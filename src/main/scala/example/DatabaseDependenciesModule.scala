package example

import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile

trait DatabaseDependenciesModule {
  this: AkkaDependenciesModule =>

  lazy val dbConfig = DatabaseConfig.forConfig[PostgresProfile]("postgres")
}
