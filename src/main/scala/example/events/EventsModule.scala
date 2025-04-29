package example.events

import example.kafka.KafkaModule
import example.{AkkaDependenciesModule, DatabaseDependenciesModule}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.pekko.kafka.ConsumerSettings
import org.apache.pekko.kafka.Subscriptions.topics
import org.apache.pekko.kafka.scaladsl.Consumer
import org.apache.pekko.stream.scaladsl.Sink

trait EventsModule {
  this: AkkaDependenciesModule with DatabaseDependenciesModule with KafkaModule =>

  lazy val repo = new EventRepository(dbConfig)


  Consumer
    .plainSource(
      settings = ConsumerSettings(
        system = system,
        keyDeserializer = new StringDeserializer,
        valueDeserializer = new StringDeserializer,
      )
        .withGroupId(consumerGroupId)
        .withBootstrapServers(bootstrapServers),
      subscription = topics(eventsTopic)
    )
    .mapAsync(parallelism = 1) { record =>
      scribe.info(s"Reading record $record")
      repo.store(
        accountId = record.key(),
        content = record.value()
      )
    }
    .runWith(Sink.ignore)
    .onComplete {
      _ => System.exit(1)
    }

}
