package example

import example.dummy_messages.DummyMessageProducerModule
import example.events.EventsModule
import example.http.routes.HttpControllersModule

trait ApplicationRootModule
  extends AkkaDependenciesModule
    with ConfigModule
    with DummyMessageProducerModule
    with EventsModule
    with HttpControllersModule
