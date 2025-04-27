package example.kafka

import example.{AkkaDependenciesModule, ConfigModule}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.pekko.Done
import org.apache.pekko.event.Logging
import org.apache.pekko.kafka.scaladsl.{Consumer, Producer}
import org.apache.pekko.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import org.apache.pekko.stream.Attributes
import org.apache.pekko.stream.scaladsl.Sink

import scala.concurrent.Future

trait KafkaModule {
  this: AkkaDependenciesModule with ConfigModule =>

  private val kafkaConfig = conf.getConfig("kafka")
  private val bootstrapServers = kafkaConfig.getStringList("bootstrap_servers").toArray().mkString(",")
  val eventsTopic = kafkaConfig.getString("events_topic")

  scribe.info(s"Kafka bootstrap servers list: [$bootstrapServers]")
  val producer: Sink[ProducerRecord[String, String], Future[Done]] = Producer
    .plainSink(
      ProducerSettings(
        system = system,
        keySerializer = new StringSerializer,
        valueSerializer = new StringSerializer
      ).withBootstrapServers(bootstrapServers)
    )

  val eventMessagesSource = Consumer.plainSource(
      settings = ConsumerSettings(
        system = system,
        keyDeserializer = new StringDeserializer,
        valueDeserializer = new StringDeserializer,
      )
        .withGroupId("abc")
        .withBootstrapServers(bootstrapServers),
      subscription = Subscriptions.topics(eventsTopic)
    )
    .withAttributes(
      Attributes(
        Attributes.LogLevels(

          onElement = Logging.InfoLevel,
          onFinish = Logging.InfoLevel,
          onFailure = Logging.ErrorLevel
        )
      )
    )
    .log(
      name = "events_logger",
      extract = msg => s"key: ${msg.key()} value: ${msg.value()}"
    )
    .map {record =>
      scribe.info("we are here")
      record

    }

    .runWith(Sink.ignore)
    .onComplete {
      _ => System.exit(1)
    }
}
