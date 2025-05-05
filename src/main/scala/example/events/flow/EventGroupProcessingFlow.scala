package example.events.flow

import example.events.EventGroupSyncService
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.{Flow, Source}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
 * Flow that processes a stream of event group ids, performs a sync operation on each group
 * and emmits the event group ids that are potentially not completely synced
 */
object EventGroupProcessingFlow {

  def apply(
             eventGroupProcessor: EventGroupSyncService,
           )(implicit ec: ExecutionContext): Flow[String, String, NotUsed] = {
    Flow[String]
      .groupedWithin(
        n = 100,
        d = 5.seconds
      )
      .map(_.toSet)
      .flatMapConcat(Source.apply)
      .mapAsyncUnordered(parallelism = 3) { eventGroup =>
        eventGroupProcessor
          .syncEventGroup(eventGroup)
          .recover(_ => false)
          .map(groupNeedsRetrying => (eventGroup, groupNeedsRetrying))
      }
      .collect {
        case (eventGroupId, needsRetry) if needsRetry => eventGroupId
      }
  }
}
