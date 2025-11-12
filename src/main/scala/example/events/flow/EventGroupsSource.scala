package example.events.flow

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.kafka.ConsumerSettings
import org.apache.pekko.kafka.Subscriptions.topics
import org.apache.pekko.kafka.scaladsl.Consumer
import org.apache.pekko.stream.scaladsl.Source

object EventGroupsSource {

  private val eventGroupsClientId = 2.toString

  def apply(
    bootstrapServers: Iterable[String],
    eventGroupsConsumerGroupId: String,
    eventGroupsTopic: String
  )(implicit system: ActorSystem): Source[String, Consumer.Control] = {
    Consumer
      .plainSource(
        settings = ConsumerSettings(
          system = system,
          keyDeserializer = new StringDeserializer,
          valueDeserializer = new StringDeserializer,
        )
      .withGroupId(eventGroupsConsumerGroupId)
      .withClientId(eventGroupsClientId)
      .withBootstrapServers(bootstrapServers.mkString(",")),
        subscription = topics(eventGroupsTopic)
      )
      .map(_.key())
  }

}
