package example.events

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.{Flow, Source}
import scala.concurrent.duration._

object EventGroupProcessingFlow {
  def apply(eventGroupProcessor: EventGroupProcessorService): Flow[ConsumerRecord[String, String], Unit, NotUsed] = {
    Flow[ConsumerRecord[String, String]]
      .groupedWithin(
        n = 100,
        d = 1.minute
      )
      .map {
        _.map(_.key()).toSet
      }
      .flatMapConcat { eventGroupsToProcess =>
        Source(eventGroupsToProcess)
          .mapAsyncUnordered(parallelism = 3) { eventGroup =>
            eventGroupProcessor
              .syncEventGroup(eventGroup)
          }
      }
  }
}
