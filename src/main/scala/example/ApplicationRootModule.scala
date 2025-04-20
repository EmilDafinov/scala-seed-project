package example

import example.dummy_messages.DummyMessageProducerModule
import example.http.routes.HttpControllersModule
import example.kafka.KafkaModule


trait ApplicationRootModule
  extends AkkaDependenciesModule
    with KafkaModule
    with DummyMessageProducerModule
    with HttpControllersModule {


}
