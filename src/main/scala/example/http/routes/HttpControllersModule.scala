package example.http.routes

import example.AkkaDependenciesModule
import example.dummy_messages.DummyMessageProducerModule
import example.kafka.KafkaModule
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route

trait HttpControllersModule {
  this: AkkaDependenciesModule
    with DummyMessageProducerModule
    with KafkaModule =>

  val routes: Route =
    HealthController() ~
      MessageProducerController(
        producer = dummyMessagedProducer,
        messageSink = kafkaProducer,
        topic = eventsTopic
      )
}
