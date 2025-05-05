package example.events.flow

import example.events.EventRepository
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.Flow

import scala.concurrent.ExecutionContext

object EventStoringFlow {
  def apply(repo: EventRepository)(implicit ec: ExecutionContext): Flow[(String, String), String, NotUsed] = {
    Flow[(String, String)]
      .mapAsync(parallelism = 1) { case (eventGroup, eventContent) =>
        repo.storeEvent(
            eventGroup = eventGroup,
            content = eventContent
          )
          .map(_ => eventGroup)
      }
  }
}
