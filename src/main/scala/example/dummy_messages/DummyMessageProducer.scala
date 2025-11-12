package example.dummy_messages

import java.util.UUID

case class EventToDeliver(
  eventGroup: String,
  content: String //possibly should be JSON
)

class DummyMessageProducer {
  private val random  = scala.util.Random
  private val tenantCount = 5
  private val customerCount = 10

  def dummyMessages(count: Int): Seq[EventToDeliver] = {
    (0 until  count)
      .map { _ =>
        val eventGroupId = s"tenant${random.nextInt(tenantCount)}:customer${random.nextInt(customerCount)}"
        EventToDeliver(
          eventGroup = eventGroupId,
          content = s"""{ "eventGroup": "$eventGroupId", "field": "${UUID.randomUUID().toString}" }"""
        )
      }
      .map {
        message =>
          scribe.info(s"Creating message: $message")
          message
      }
  }
}
