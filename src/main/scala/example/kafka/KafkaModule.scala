package example.kafka

import example.{AkkaDependenciesModule, ApplicationRootModule, ConfigModule}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.pekko.Done
import org.apache.pekko.kafka.ProducerSettings
import org.apache.pekko.kafka.scaladsl.Producer
import org.apache.pekko.stream.scaladsl.{Sink, Source}

import scala.concurrent.Future

trait KafkaModule {
  this: AkkaDependenciesModule with ConfigModule =>

  private val kafkaConfig = conf.getConfig("kafka")
  private val bootstrapServers = kafkaConfig.getStringList("bootstap_servers").toArray()

  val producer: Sink[ProducerRecord[String, String], Future[Done]] = Producer
    .plainSink(
      ProducerSettings(
        system = system,
        keySerializer = new StringSerializer,
        valueSerializer = new StringSerializer
      ).withBootstrapServers(bootstrapServers.mkString(","))
    )
}
