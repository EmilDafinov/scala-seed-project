package example.events


import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.Sink.ignore
import org.apache.pekko.stream.scaladsl.Source

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

/**
 * Processes the unpublished events for a give
 */
class EventGroupSyncService(
  eventDeliveryService: EvenDeliveryService,
  eventRepository: EventRepository,
  maxBatchSize: Int = 10

)(implicit ec: ExecutionContext, system: ActorSystem) {


  /**
   * Attempt to deliver as many of the undelivered messages
   * from a single event group as possible
   *
   * @param eventGroup the id of the event group whose messages we want to process
   * @return whether or not all messages in the group were processed
   */
  def syncEventGroup(eventGroup: String): Future[Boolean] = {
    for {
      undeliveredEventsBatch <- eventRepository.readUnsentFor(
        eventGroup = eventGroup,
        batchSize = maxBatchSize
      )

      batchSuccessfullyDelivered <- Source(undeliveredEventsBatch)
        .mapAsync(1) { case (currentEventId, unsentEvent) =>
          eventDeliveryService
            .deliverEvent(
              eventId = currentEventId,
              eventContent = unsentEvent
            )
            .recoverWith {
              case NonFatal(ex) =>
                scribe.error(s"Failed sending event with id $currentEventId for group $eventGroup", ex)
                eventRepository.markGroupEventsAsSuccessful(
                  eventGroup = eventGroup,
                  untilEventId = currentEventId - 1
                )
                throw ex
            }
        }
        .runWith(ignore)
        .map(_ => true)
        .recover(_ => false)

      _ <- undeliveredEventsBatch.lastOption
        .filter(_ => batchSuccessfullyDelivered)
        .map { case (lastEventId, _) =>
          eventRepository.markGroupEventsAsSuccessful(
            eventGroup = eventGroup,
            untilEventId = lastEventId
          )
        }.getOrElse(Future.successful())

      eventGroupHasMoreUndeliveredMessages = !batchSuccessfullyDelivered || undeliveredEventsBatch.size == maxBatchSize
      _ = scribe.info(s"$eventGroup has more undelivered messages: $eventGroupHasMoreUndeliveredMessages")
    } yield eventGroupHasMoreUndeliveredMessages
  }
}
