package example

import example.InputListeners.{startHttpServer, startListeningToEventGroupsTopic, startListeningToEventTopic}

object Main extends ApplicationRootModule {

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
