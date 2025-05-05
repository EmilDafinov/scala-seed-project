package example.http.routes

import example.AkkaDependenciesModule
import example.dummy_messages.DummyMessageProducerModule
import example.events.EventsModule
import example.events.flow.EventSink
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route

trait HttpControllersModule {
  this: AkkaDependenciesModule
    with DummyMessageProducerModule
    with EventsModule =>

  val routes: Route =
    HealthController() ~
      MessageProducerController(
        producer = dummyMessagedProducer,
        messageSink = EventSink(eventsTopic, bootstrapServers)
      )
}
