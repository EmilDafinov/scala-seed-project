package example.events

import scala.concurrent.Future

class EvenDeliveryService {
  /**
   * deliver an event payload to its external destination
   */
  def deliverEvent(eventId: Long, eventContent: String): Future[Unit] = {
    scribe.info(s"Delivering event with id [$eventId] and content [$eventContent]")
    Future.successful()
  }
}
