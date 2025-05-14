package example.events

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class EvenDeliveryService {
  private val random: Random = new Random()
  /**
   * deliver an event payload to its external destination
   */
  def deliverEvent(eventId: Long, eventContent: String)(implicit ec: ExecutionContext): Future[Unit] = Future {
    if (random.nextInt(n = 100) < 90)
      scribe.info(s"Delivering event with id [$eventId] and content [$eventContent]")
    else
      throw new RuntimeException(s"Failed delivering event $eventId")
  }
}
