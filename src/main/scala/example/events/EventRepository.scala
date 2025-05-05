package example.events

import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class EventRepository(dbConfig: DatabaseConfig[PostgresProfile])(implicit ec: ExecutionContext) {

  /**
   * Stores the content of an event for a given event group.
   * An event group designates a set of events that must be processed
   * sequentially, in the order that they were inserted in the db
   *
   * @param eventGroup the group identifier of the event group.
   * @param content    the event content
   * @return
   */
  def storeEvent(eventGroup: String, content: String): Future[Int] = {
    scribe.info(s"Storing record $content for group $eventGroup")
    dbConfig.db.run(
      sqlu"""
        INSERT INTO events (event_group, content, external_id)
        VALUES ($eventGroup, $content ::jsonb, ${UUID.randomUUID().toString}:: uuid)
      """
    )
  }

  /**
   *
   * @param eventGroup the event group the unsent events belong to.
   * @return the unsent event ids and payloads IN THE ORDER THAT THEY SHOULD BE DELIVERED
   *         (in ascending order, by id)
   */
  def readUnsentFor(eventGroup: String, batchSize: Int): Future[Vector[(Long, String)]] =
    dbConfig.db.run(
      sql"""
        SELECT id, content
        FROM events
        WHERE event_group = $eventGroup
          AND delivered IS FALSE
        ORDER BY id
        LIMIT $batchSize
      """.as[(Long, String)]
    )

  /**
   * To be called when the last event of a batch ahs been successfully processed.
   * This would mark all events including the current one as successfully delivereed
   *
   * @param eventGroup   the key whose events are being processed
   * @param untilEventId id of the last event of a successfully processed batch
   * @return
   */
  def markGroupEventsAsSuccessful(eventGroup: String, untilEventId: Long): Future[Unit] =
    dbConfig.db.run(
      sqlu"""
        UPDATE events
        SET delivered = TRUE
        WHERE event_group = $eventGroup
          AND id <= $untilEventId
      """
    ).map(_ => ())
}
