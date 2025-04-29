package example.events

import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

class EventRepository(dbConfig: DatabaseConfig[PostgresProfile]) {

  def store(accountId: String, content: String) = {
    dbConfig.db.run(
      sqlu"""
             INSERT INTO events (account_id, content)
             VALUES ($accountId, $content ::jsonb)
          """
    )
  }
}
