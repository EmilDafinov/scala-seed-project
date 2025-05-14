package example

import example.InputListeners.{startHttpServer, startListeningToEventGroupsTopic, startListeningToEventTopic}
import example.events.EventsModule
import example.http.routes.HttpControllersModule

object Main
  extends AkkaDependenciesModule
     with EventsModule
     with HttpControllersModule {

  def main(args: Array[String]): Unit = {

    startHttpServer

    startListeningToEventTopic(
      eventsSource = eventsSource,
      eventStoringFlow = eventStoringFlow,
      eventGroupSink = eventGroupSink
    )

    startListeningToEventGroupsTopic(
      eventGroupsSource = eventGroupsSource,
      eventGroupSyncingFlow = eventGroupSyncingFlow,
      eventGroupSink = eventGroupSink
    )
  }
}
