package example.events.flow

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.pekko.Done
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.kafka.ProducerSettings
import org.apache.pekko.kafka.scaladsl.Producer
import org.apache.pekko.stream.scaladsl.{Flow, Keep, Sink}

import scala.concurrent.Future

object EventGroupSink {
  def apply(eventGroupsTopic: String, bootstrapServers: Iterable[String])(implicit system: ActorSystem): Sink[String, Future[Done]] = {
    Flow[String]
      .map(eventGroup => new ProducerRecord(eventGroupsTopic, eventGroup, eventGroup))
      .toMat(
        Producer
          .plainSink(
            ProducerSettings(
              system = system,
              keySerializer = new StringSerializer,
              valueSerializer = new StringSerializer
            ).withBootstrapServers(bootstrapServers.mkString(","))
          )
      )(Keep.right)
  }
}