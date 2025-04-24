package example.http.routes
import example.AkkaDependenciesModule
import example.dummy_messages.DummyMessageProducerModule
import example.kafka.KafkaModule
import org.apache.pekko.http.scaladsl.server.Directives._

trait HttpControllersModule {
  this: AkkaDependenciesModule
    with DummyMessageProducerModule
    with KafkaModule =>

  val routes = HealthController() ~
    MessageProducerController(
      producer = dummyMessagedProducer,
      messageSink = producer,
      topic = eventsTopic
    )
}
