package example

import example.dummy_messages.DummyMessageProducerModule
import example.events.EventsModule
import example.http.routes.HttpControllersModule
import example.kafka.KafkaModule


trait ApplicationRootModule
  extends AkkaDependenciesModule
    with ConfigModule
    with KafkaModule
    with DummyMessageProducerModule
    with DatabaseDependenciesModule
    with EventsModule
    with HttpControllersModule {


}
