package example.dummy_messages

import java.util.UUID

case class WebhookMessage(
  key: String,
  content: String //possibly should be JSON 
                         
)

class DummyMessageProducer {
  private val random  = scala.util.Random
  private val accounts = List(
    "account1",
    "account2",
    "account3",
    "account4",
    "account5",
  )
  def dummyMessages(count: Int = 100): Seq[WebhookMessage] = {
    (1 to count)
      .map(_ =>
        WebhookMessage(
          key = accounts(random.nextInt(accounts.size - 1)),
          content = s"""{ "dummy": "payload", "field": "${UUID.randomUUID().toString}" }"""
        )
      ).map {
        message =>
          scribe.info(s"Creating message: $message")
          message
      }
  }
}
