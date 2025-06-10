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
      Given("a test event group and a dummy event")
      val testEventGroup = "testEventGroup"
      val testContent = "{}"

      When("we store a single event and give read it back")
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
      Given("a test event group and a dummy event")
      val testEventGroup = "testEventGroup"
      val testContent1 = """{"event": 1}"""
      val testContent2 = """{"event": 2}"""
      val testContent3 = """{"event": 3}"""

      When("we store a 3 event and give read it back")
      val actualFirstBatch = (for {
        _ <- tested.storeEvent(eventGroup = testEventGroup, content = testContent1)
        _ <- tested.storeEvent(eventGroup = testEventGroup, content = testContent2)
        _ <- tested.storeEvent(eventGroup = testEventGroup, content = testContent3)
        firstBatch <- tested.readUnsentFor(eventGroup = testEventGroup, batchSize = 2)
      } yield (firstBatch)).futureValue

      Then("the event read should match the event written")
      actualFirstBatch.map(_._2) should contain theSameElementsInOrderAs List(testContent1, testContent2)
    }
  }
}
