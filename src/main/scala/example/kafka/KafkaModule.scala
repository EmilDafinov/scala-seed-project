package example.kafka

import example.events.EventsModule
import example.{AkkaDependenciesModule, ConfigModule}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.pekko.Done
import org.apache.pekko.kafka.ProducerSettings
import org.apache.pekko.kafka.scaladsl.Producer
import org.apache.pekko.stream.scaladsl.Sink

import scala.concurrent.Future
import scala.jdk.CollectionConverters._

trait KafkaModule {
  this: AkkaDependenciesModule with ConfigModule with EventsModule =>

  private val kafkaConfig = conf.getConfig("kafka")
  lazy val bootstrapServers = kafkaConfig.getStringList("bootstrap_servers").asScala
  lazy val eventsTopic = kafkaConfig.getString("events_topic")
  lazy val consumerGroupId = kafkaConfig.getString("consumer_group_id")

  scribe.info(s"Kafka bootstrap servers list: [$bootstrapServers]")
  lazy val producer: Sink[ProducerRecord[String, String], Future[Done]] = Producer
    .plainSink(
      ProducerSettings(
        system = system,
        keySerializer = new StringSerializer,
        valueSerializer = new StringSerializer
      ).withBootstrapServers(bootstrapServers.mkString(","))
    )
}
