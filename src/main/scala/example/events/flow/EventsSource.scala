package example.events.flow

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.kafka.ConsumerSettings
import org.apache.pekko.kafka.Subscriptions.topics
import org.apache.pekko.kafka.scaladsl.Consumer
import org.apache.pekko.stream.scaladsl.Source

object EventsSource {

  def apply(
             bootstrapServers: Iterable[String],
             eventsConsumerGroupId: String,
             eventsTopic: String
           )(implicit system: ActorSystem): Source[(String, String), Consumer.Control] = {
    Consumer
      .plainSource(
        settings = ConsumerSettings(
          system = system,
          keyDeserializer = new StringDeserializer,
          valueDeserializer = new StringDeserializer,
        )
          .withGroupId(eventsConsumerGroupId)
          .withClientId(1.toString)
          .withBootstrapServers(bootstrapServers.mkString(",")),
        subscription = topics(eventsTopic)
      )
      .map(consumerRecord => (consumerRecord.key(), consumerRecord.value()))
  }

}
