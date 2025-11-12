package example.events.flow

import example.events.EventGroupSyncService
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.OverflowStrategy.backpressure
import org.apache.pekko.stream.scaladsl.{Flow, Source}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
 * Flow that processes a stream of event group ids, performs a sync operation on each group
 * and emits the event group ids that are potentially not completely synced
 */
object EventGroupProcessingFlow {

  def apply(
             eventGroupProcessor: EventGroupSyncService,
           )(implicit ec: ExecutionContext): Flow[String, String, NotUsed] = {
    Flow[String]
      .groupedWithin(
        n = 100,
        d = 3.seconds
      )
      .map(_.toSet)
      .map { groupsBatch =>
        scribe.info(s"Event group batch to process: $groupsBatch")
        groupsBatch
      }
      .flatMapConcat(Source.apply)
      .buffer(size = 20, overflowStrategy = backpressure)
      .mapAsyncUnordered(parallelism = 10) { eventGroup =>
        scribe.info(s"Starting sync of event group [$eventGroup]")
        eventGroupProcessor
          .syncEventGroup(eventGroup)
          .recover(_ => false)
          .map { allEventsInGroupProcessed =>
            scribe.info(s"Event group batch from : $eventGroup completed; All events processed: $allEventsInGroupProcessed")
            eventGroup -> allEventsInGroupProcessed
          }
      }
      .buffer(size = 300, overflowStrategy = backpressure)
      .collect {
        case (eventGroupId, needsRetry) if needsRetry =>
          scribe.info(s"Retrying event group $eventGroupId")
          eventGroupId
      }
  }
}
