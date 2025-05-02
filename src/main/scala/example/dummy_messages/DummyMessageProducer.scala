package example.dummy_messages

import java.util.UUID

case class EventToDeliver(
  eventGroup: String,
  content: String //possibly should be JSON
)

class DummyMessageProducer {
  private val random  = scala.util.Random
  private val eventGroups = List(
    "account1",
    "account2",
    "account3",
    "account4",
    "account5",
  )
  def dummyMessages(count: Int = 100): Seq[EventToDeliver] = {
    (0 until  count)
      .map { _ =>
        val eventGroupId = eventGroups(random.nextInt(eventGroups.size))
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
