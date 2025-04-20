package example.dummy_messages

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
          content = """{ "dummy": "payload" }"""
        )
      )
  }

//  def produceMessages(implicit mat: Materializer) = {
//    Source
//      .fromIterator(() =>
//        (1 to 100)
//          .map(_ =>
//            new ProducerRecord(
//              "event_stream_1_topic",
//              "accountId1",
//              ""
//            )
//          ).iterator
//      ).runWith(messageSink)
//  }
}
