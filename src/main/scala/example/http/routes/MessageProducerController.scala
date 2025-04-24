package example.http.routes

import example.dummy_messages.DummyMessageProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.pekko.Done
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.model.{HttpResponse, StatusCodes}
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.stream.scaladsl.{Sink, Source}

import scala.concurrent.{ExecutionContext, Future}


object MessageProducerController {

  def apply(
     producer: DummyMessageProducer,
     topic: String,
     messageSink: Sink[ProducerRecord[String, String], Future[Done]]
  )(implicit sys: ActorSystem, ec: ExecutionContext): Route = {
    (path("messages")
      & post) {
      complete(
        Source
          .fromIterator(() => producer.dummyMessages().iterator)
          .map { msg =>
            new ProducerRecord(topic, msg.key, msg.content)
          }
          .runWith(messageSink)
          .map(_ => HttpResponse(status = StatusCodes.Accepted))
      )
    }
  }
}
