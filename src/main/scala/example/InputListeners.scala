package example

import example.Main.routes
import org.apache.pekko.{Done, NotUsed}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.kafka.scaladsl.Consumer
import org.apache.pekko.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object InputListeners {

  def startHttpServer(implicit system: ActorSystem, ec: ExecutionContext) = {
    Http()
      .newServerAt("0.0.0.0", 9000)
      .bind(routes)
      .andThen {
        case Success(binding) =>
          scribe.info(s"Server started at ${binding.localAddress}")
        case Failure(exception) =>
          scribe.error("Failed starting http server, shutting down application", exception)
          System.exit(1)
      }
  }

  def startListeningToEventTopic(
                                  eventsSource: Source[(String, String), Consumer.Control],
                                  eventStoringFlow: Flow[(String, String), String, NotUsed],
                                  eventGroupSink: Sink[String, Future[Done]]
                                )(implicit system: ActorSystem, ec: ExecutionContext) = {
    eventsSource
      .via(eventStoringFlow)
      .runWith(eventGroupSink)
      .onComplete(_ => System.exit(1))
  }

  def startListeningToEventGroupsTopic(
                                        eventGroupsSource: Source[String, Consumer.Control],
                                        eventGroupSyncingFlow: Flow[String, String, NotUsed],
                                        eventGroupSink: Sink[String, Future[Done]]
                                      )(implicit system: ActorSystem, ec: ExecutionContext) = {
    eventGroupsSource
      .via(eventGroupSyncingFlow)
      .runWith(eventGroupSink)
      .onComplete(_ => System.exit(1))
  }
}
