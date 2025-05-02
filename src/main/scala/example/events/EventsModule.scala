package example.events

import example.kafka.KafkaModule
import example.{AkkaDependenciesModule, DatabaseDependenciesModule}
import org.apache.pekko.stream.scaladsl.Sink

trait EventsModule {
  this: AkkaDependenciesModule with DatabaseDependenciesModule with KafkaModule =>

  private lazy val eventGroupsTopic = kafkaConfig.getString("keys")
  private lazy val eventsConsumerGroupId = kafkaConfig.getString("events_consumer_group_id")
  private lazy val keysConsumerGroupId = kafkaConfig.getString("event_keys_consumer_group_id")

  lazy val repo: EventRepository = new EventRepository(dbConfig)

  lazy val eventGroupProcessor: EventGroupProcessorService = new EventGroupProcessorService(
    eventDeliveryService = new EvenDeliveryService,
    eventRepository = repo
  )

  val eventProcessors = (0 to 1)
    .map { clientId =>
      TopicConsumer(
        bootstrapServers = bootstrapServers,
        consumerGroupId = eventsConsumerGroupId,
        clientId = clientId.toString,
        topic = eventsTopic
      )
      .via(
        EventToEventGroupFlow(
          repo = repo,
          eventGroupsTopic = eventGroupsTopic
        )
      )
    }
    .foreach(
      _.runWith(kafkaProducer)
        .onComplete(_ => System.exit(1))
    )

  (0 to 1)
    .map { clientId =>
      TopicConsumer(
        bootstrapServers = bootstrapServers,
        consumerGroupId = keysConsumerGroupId,
        clientId = clientId.toString,
        topic = eventGroupsTopic
      )
      .via(EventGroupProcessingFlow(eventGroupProcessor))
    }
    .foreach(
      _.runWith(Sink.ignore)
        .onComplete(_ => System.exit(1))
    )
}
