package example.http.routes
import example.dummy_messages.DummyMessageProducerModule
import example.kafka.KafkaModule
import example.{AkkaDependenciesModule, ApplicationRootModule}
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route

trait HttpControllersModule {
  this: AkkaDependenciesModule
    with DummyMessageProducerModule
    with KafkaModule =>

  val routes = HealthController() ~
    MessageProducerController(
      producer = dummyMessagedProducer,
      messageSink = producer
    )
}
