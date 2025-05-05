package example.events

import com.typesafe.config.Config
import example.events.flow.{EventGroupProcessingFlow, EventGroupSink, EventGroupsSource, EventStoringFlow, EventsSource}
import example.{AkkaDependenciesModule, ConfigModule}
import org.apache.pekko.{Done, NotUsed}
import org.apache.pekko.kafka.scaladsl.Consumer
import org.apache.pekko.stream.scaladsl.{Flow, Sink, Source}
import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile

import scala.concurrent.Future
import scala.jdk.CollectionConverters._

trait EventsModule {
  this: AkkaDependenciesModule with ConfigModule =>

  lazy val dbConfig = DatabaseConfig.forConfig[PostgresProfile]("postgres")

  private val kafkaConfig: Config = conf.getConfig("kafka")
  val eventsTopic: String = kafkaConfig.getString("events")
  val bootstrapServers: List[String] = kafkaConfig.getStringList("bootstrap_servers").asScala.toList

  scribe.info(s"Kafka bootstrap servers list: [$bootstrapServers]")
  private lazy val eventGroupsTopic = kafkaConfig.getString("keys")
  private lazy val eventsConsumerGroupId = kafkaConfig.getString("events_consumer_group_id")
  private lazy val eventGroupsConsumerGroupId = kafkaConfig.getString("event_groups_consumer_group_id")

  private lazy val eventRepository: EventRepository = new EventRepository(dbConfig)

  lazy val eventGroupProcessor: EventGroupSyncService = new EventGroupSyncService(
    eventDeliveryService = new EvenDeliveryService,
    eventRepository = eventRepository
  )

  val eventGroupSink: Sink[String, Future[Done]] = EventGroupSink(
    eventGroupsTopic = eventGroupsTopic,
    bootstrapServers = bootstrapServers,
  )

  val eventsSource: Source[(String, String), Consumer.Control] = EventsSource(
    bootstrapServers = bootstrapServers,
    eventsConsumerGroupId = eventsConsumerGroupId,
    eventsTopic = eventsTopic
  )

  val eventGroupsSource: Source[String, Consumer.Control] = EventGroupsSource(
    bootstrapServers = bootstrapServers,
    keysConsumerGroupId = eventGroupsConsumerGroupId,
    eventGroupsTopic = eventGroupsTopic
  )

  val eventStoringFlow: Flow[(String, String), String, NotUsed] = EventStoringFlow(eventRepository)

  val eventGroupSyncingFlow: Flow[String, String, NotUsed] = EventGroupProcessingFlow(eventGroupProcessor)
}
