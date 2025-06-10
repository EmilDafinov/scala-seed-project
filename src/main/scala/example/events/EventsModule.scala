package example.events

import com.typesafe.config.{Config, ConfigFactory}
import example.AkkaDependenciesModule
import example.events.flow._
import org.apache.pekko.kafka.scaladsl.Consumer
import org.apache.pekko.stream.scaladsl.{Flow, Sink, Source}
import org.apache.pekko.{Done, NotUsed}
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.Future
import scala.jdk.CollectionConverters._

trait EventsModule {
  this: AkkaDependenciesModule =>

  lazy val db = Database.forConfig(
    path = "postgres.db"
  )
  private lazy val config: Config = ConfigFactory.load()
  private lazy val kafkaConfig: Config = config.getConfig("kafka")
  lazy val eventsTopic: String = kafkaConfig.getString("events")
  lazy val bootstrapServers: List[String] = kafkaConfig.getStringList("bootstrap_servers").asScala.toList

  scribe.info(s"Kafka bootstrap servers list: [$bootstrapServers]")
  private lazy val eventGroupsTopic = kafkaConfig.getString("keys")
  private lazy val eventsConsumerGroupId = kafkaConfig.getString("events_consumer_group_id")
  private lazy val eventGroupsConsumerGroupId = kafkaConfig.getString("event_groups_consumer_group_id")

  private lazy val eventRepository: EventRepository = new EventRepository(db)

  lazy val eventGroupProcessor: EventGroupSyncService = new EventGroupSyncService(
    eventDeliveryService = new EvenDeliveryService,
    eventRepository = eventRepository
  )

  lazy val eventGroupSink: Sink[String, Future[Done]] = EventGroupSink(
    eventGroupsTopic = eventGroupsTopic,
    bootstrapServers = bootstrapServers,
  )

  lazy val eventsSource: Source[(String, String), Consumer.Control] = EventsSource(
    bootstrapServers = bootstrapServers,
    eventsConsumerGroupId = eventsConsumerGroupId,
    eventsTopic = eventsTopic
  )

  lazy val eventGroupsSource: Source[String, Consumer.Control] = EventGroupsSource(
    bootstrapServers = bootstrapServers,
    eventGroupsConsumerGroupId = eventGroupsConsumerGroupId,
    eventGroupsTopic = eventGroupsTopic
  )

  lazy val eventStoringFlow: Flow[(String, String), String, NotUsed] = EventStoringFlow(eventRepository)

  lazy val eventGroupSyncingFlow: Flow[String, String, NotUsed] = EventGroupProcessingFlow(eventGroupProcessor)
}
