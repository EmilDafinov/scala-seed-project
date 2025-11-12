package example.events.flow

import example.events.EventRepository
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.{Flow, Source}

import scala.concurrent.duration._

object EventStoringFlow {
  def apply(repo: EventRepository): Flow[(String, String), String, NotUsed] = {
    Flow[(String, String)]
      .groupedWithin(1000, 1.seconds)
      .mapAsync(parallelism = 1) { eventBatch =>
        repo.storeEvents(eventBatch)
      }
      .flatMapConcat(Source.apply)
  }
}
