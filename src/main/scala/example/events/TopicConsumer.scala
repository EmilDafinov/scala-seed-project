package example.events

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.kafka.ConsumerSettings
import org.apache.pekko.kafka.Subscriptions.topics
import org.apache.pekko.kafka.scaladsl.Consumer
import org.apache.pekko.stream.scaladsl.Source

object TopicConsumer {
  def apply(
             consumerGroupId: String,
             clientId: String,
             bootstrapServers: Iterable[String],
             topic: String
           )(implicit system: ActorSystem): Source[ConsumerRecord[String, String], Consumer.Control] = {
    Consumer
      .plainSource(
        settings = ConsumerSettings(
          system = system,
          keyDeserializer = new StringDeserializer,
          valueDeserializer = new StringDeserializer,
        )
          .withGroupId(consumerGroupId)
          .withClientId(clientId)
          .withBootstrapServers(bootstrapServers.mkString(",")),
        subscription = topics(topic)
      )
  }
}
