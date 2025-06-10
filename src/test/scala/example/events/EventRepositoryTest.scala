package example.events

import slick.jdbc.JdbcBackend.Database

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}
import scala.language.postfixOps

class EventRepositoryTest extends UnitTestSpec {

  private lazy val dbConfig = Database.forConfig("postgres.db")
  lazy val tested = new EventRepository(dbConfig)

  override implicit def patienceConfig: PatienceConfig =  PatienceConfig(
    timeout = scaled(5500 millis),
    interval = scaled(15 millis)
  )
  import slick.jdbc.PostgresProfile.api._

  override def beforeEach(): Unit = {
    Await.result(
      awaitable = dbConfig.run(sqlu"DELETE FROM events"),
      atMost = Duration.Inf
    )
  }

  override def afterAll(): Unit = {
    super.afterAll()
    dbConfig.close()
  }

  "EventRepository" should {
    "store an event and read it back " in {
      Given("a test event group")
      val testEventGroup = "testEventGroup"
      And("a dummy event")
      val testContent = "{}"

      When("we store the dummy event in the test event group")
      And("read a batch of events from the test group")
      val actualEventsRead = (for {
        _ <- tested.storeEvent(eventGroup = testEventGroup, content = testContent)
        eventsRead <- tested.readUnsentFor(eventGroup = testEventGroup, batchSize = 10)
      } yield eventsRead).futureValue

      Then("the event read should match the event written")
      actualEventsRead should have size 1
      val (_, actualEventContent) = actualEventsRead.head
      actualEventContent shouldEqual testContent
    }

    "store events and read only up to the specified batch size" in {
      Given("a test event group and an 4 events")
      val testEventGroup = "testEventGroup"
      val testEvent1 = """{"event": 1}"""
      val testEvent2 = """{"event": 2}"""
      val testEvent3 = """{"event": 3}"""
      val testEventFromAnotherGroup = """{"event": 4}"""
      val expectedEventBatch = List(testEvent1, testEvent2)

      When("we store 3 events in the test group and 1 in another group")
      val actualEventBatchRead = (for {
        _ <- tested.storeEvent(eventGroup = testEventGroup, content = testEvent1)
        _ <- tested.storeEvent(eventGroup = testEventGroup, content = testEvent2)
        _ <- tested.storeEvent(eventGroup = testEventGroup, content = testEvent3)
        _ <- tested.storeEvent(eventGroup = "some_other_group", content = testEventFromAnotherGroup)
        firstBatch <- tested.readUnsentFor(eventGroup = testEventGroup, batchSize = 2)
      } yield firstBatch.map(_._2)).futureValue

      Then("the events should be read back in insertion order")
      And("reading back events from the test group should not exceed the batch size")
      actualEventBatchRead should contain theSameElementsInOrderAs expectedEventBatch
    }
  }
}
