package example.events.flow

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.kafka.ConsumerSettings
import org.apache.pekko.kafka.Subscriptions.topics
import org.apache.pekko.kafka.scaladsl.Consumer
import org.apache.pekko.stream.scaladsl.Source

object EventGroupsSource {

  def apply(
             bootstrapServers: Iterable[String],
             keysConsumerGroupId: String,
             eventGroupsTopic: String
           )(implicit system: ActorSystem): Source[String, Consumer.Control] = {
    Consumer
      .plainSource(
        settings = ConsumerSettings(
          system = system,
          keyDeserializer = new StringDeserializer,
          valueDeserializer = new StringDeserializer,
        )
          .withGroupId(keysConsumerGroupId)
          .withClientId(2.toString)
          .withBootstrapServers(bootstrapServers.mkString(",")),
        subscription = topics(eventGroupsTopic)
      )
      .map(_.key())
  }

}
