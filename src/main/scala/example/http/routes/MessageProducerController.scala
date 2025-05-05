package example.http.routes

import example.dummy_messages.{DummyMessageProducer, EventToDeliver}
import org.apache.pekko.Done
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.model.HttpResponse
import org.apache.pekko.http.scaladsl.model.StatusCodes.Accepted
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.stream.scaladsl.{Sink, Source}

import scala.concurrent.{ExecutionContext, Future}

object MessageProducerController {

  def apply(
     producer: DummyMessageProducer,
     messageSink: Sink[EventToDeliver, Future[Done]]
  )(implicit sys: ActorSystem, ec: ExecutionContext): Route = {
    (path("messages")
      & post
      & parameter("count".as[Int].withDefault(100))) { messageCount =>
      complete(
        Source(producer.dummyMessages(count = messageCount))
          .runWith(messageSink)
          .map(_ => HttpResponse(status = Accepted))
      )
    }
  }
}
