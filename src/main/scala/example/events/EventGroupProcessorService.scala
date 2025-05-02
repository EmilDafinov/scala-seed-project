package example.events


import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.Sink.ignore
import org.apache.pekko.stream.scaladsl.Source

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

/**
 * Processes the unpublished events for a give
 */
class EventGroupProcessorService(
  eventDeliveryService: EvenDeliveryService,
  eventRepository: EventRepository
)(implicit ec: ExecutionContext, system: ActorSystem) {

  /**
   * Attempt to deliver as many of the undelivered messages
   * from a single event group as possible
   *
   * @param eventGroup the id of the event group whose messages we want to process
   * @return
   */
  def syncEventGroup(eventGroup: String): Future[Unit] = {
    for {
      unsentEvents <- eventRepository.readUnsentFor(eventGroup)

      //TODO: This stream should terminate on failure to deliver a message
      _ <- Source(unsentEvents)
        .map { case (currentEventId, unsentEvent) =>
          eventDeliveryService
            .deliverEvent(
              eventId = currentEventId,
              eventContent = unsentEvent
            )
            .recoverWith {
              case NonFatal(ex) =>
                scribe.error(s"Failed sending event with id $currentEventId", ex)
                eventRepository.markGroupEventsAsSuccessful(
                  eventGroup = eventGroup,
                  untilEventId = currentEventId - 1
                )
                throw ex
            }
        }
        .runWith(ignore)
      _ <- unsentEvents.lastOption
        .map { case (lastEventId, _) =>
          eventRepository.markGroupEventsAsSuccessful(
            eventGroup = eventGroup,
            untilEventId = lastEventId
          )
        }.getOrElse(Future.successful())
    } yield ()
  }
}
