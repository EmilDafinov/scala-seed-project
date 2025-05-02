package example.kafka

import com.typesafe.config.Config
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
  this: AkkaDependenciesModule with ConfigModule =>

  val kafkaConfig: Config = conf.getConfig("kafka")
  val eventsTopic: String = kafkaConfig.getString("events")
  val bootstrapServers: List[String] = kafkaConfig.getStringList("bootstrap_servers").asScala.toList

  scribe.info(s"Kafka bootstrap servers list: [$bootstrapServers]")
  lazy val kafkaProducer: Sink[ProducerRecord[String, String], Future[Done]] = Producer
    .plainSink(
      ProducerSettings(
        system = system,
        keySerializer = new StringSerializer,
        valueSerializer = new StringSerializer
      ).withBootstrapServers(bootstrapServers.mkString(","))
    )
}
