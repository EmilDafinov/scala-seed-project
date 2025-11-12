package example.events

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class EvenDeliveryService(implicit ec: ExecutionContext) {
  private val random: Random = new Random()
  /**
   * deliver an event payload to its external destination
   */
  def deliverEvent(eventId: Long, eventContent: String): Future[Unit] = Future {

    Thread.sleep(random.nextInt(3) * 1000)

    if (random.nextInt(n = 100) < 90) {
      //Complete successfully
    } else
      throw new RuntimeException(s"Failed delivering event $eventId")
  }
}
