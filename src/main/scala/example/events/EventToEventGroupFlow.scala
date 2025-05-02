package example.events

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.pekko.stream.scaladsl.Flow

import scala.concurrent.ExecutionContext

object EventToEventGroupFlow {
  def apply(repo: EventRepository, eventGroupsTopic: String)(implicit ec: ExecutionContext) = {
    Flow[ConsumerRecord[String, String]]
      .mapAsync(parallelism = 1) { eventRecord =>
        scribe.info(s"Storing record $eventRecord")
        repo.storeEvent(
            eventGroup = eventRecord.key(),
            content = eventRecord.value()
          )
          .map(_ => eventRecord.key())
      }
      .map { eventGroupId =>
        new ProducerRecord(eventGroupsTopic, eventGroupId, eventGroupId)
      }
  }
}
