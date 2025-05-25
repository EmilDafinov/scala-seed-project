package example.events

import org.apache.pekko.actor.ActorSystem
import org.mockito.ArgumentMatchers.{any, anyLong, anyString}
import org.mockito.{ArgumentCaptor, ArgumentMatchers, Mockito}
import org.mockito.Mockito.{never, verify, when}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._


trait UnitTestSpec extends AnyWordSpec
  with GivenWhenThen
  with Matchers
  with ScalaFutures
  with BeforeAndAfterEach
  with BeforeAndAfterAll
  with MockitoSugar

class EventGroupSyncServiceTest extends UnitTestSpec {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(150.milliseconds),
    interval = scaled(15.milliseconds),
  )
  implicit val system = ActorSystem("test")

  private val eventDeliveryServiceMock: EvenDeliveryService = mock[EvenDeliveryService]
  private val eventRepositoryMock: EventRepository = mock[EventRepository]
  private val testMaxBatchSize: Int = 5

  val tested = new EventGroupSyncService(
    eventDeliveryService = eventDeliveryServiceMock  ,
    eventRepository = eventRepositoryMock,
    maxBatchSize = testMaxBatchSize
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(eventDeliveryServiceMock, eventRepositoryMock)
  }

  "EventGroupProcessorService" should {

    "return false (no more remaining elements in group) when batch size smaller than max size and delivered successfully" in {

      Given("an event group containing a number of events smaller than the max batch size that is delivered successfully")
      val testEventGroup = "test-group"

      val event1 = (1L, "{1}")
      val event2 = (3L, "{2}")
      val event3 = (5L, "{3}")

      when {
        eventRepositoryMock.readUnsentFor(
          eventGroup = testEventGroup,
          batchSize = testMaxBatchSize
        )
      } thenReturn {
        Future.successful(
          Vector(
            event1,
            event2,
            event3,
          )
        )
      }

      when {
        eventRepositoryMock.markGroupEventsAsSuccessful(
          eventGroup = testEventGroup,
          untilEventId = event3._1
        )
      } thenReturn {
        Future.successful()
      }

      when {
        eventDeliveryServiceMock.deliverEvent(
          eventId = anyLong(),
          eventContent = anyString()
        )(any[ExecutionContext])
      } thenReturn Future.successful()

      When("trying to sync the test group")
      val actual = tested.syncEventGroup(testEventGroup).futureValue

      Then("we attempt to deliver each event and we return that there are no more unsent events in the event group")
      actual shouldBe false
      verify(eventDeliveryServiceMock).deliverEvent(eventId = event1._1, eventContent = event1._2)
      verify(eventDeliveryServiceMock).deliverEvent(eventId = event2._1, eventContent = event2._2)
      verify(eventDeliveryServiceMock).deliverEvent(eventId = event3._1, eventContent = event3._2)
      verify(eventRepositoryMock).markGroupEventsAsSuccessful(
        eventGroup = testEventGroup,
        untilEventId = event3._1
      )
    }

    "return true (not all elements in group delivered) when an event fails to deliver" in {

      Given("an event group containing a number of events smaller than the max batch size that is delivered successfully")
      val testEventGroup = "test-group"

      val event1 = (1L, "{1}")
      val event2 = (3L, "{2}")
      val event3 = (5L, "{3}")

      when {
        eventRepositoryMock.readUnsentFor(
          eventGroup = testEventGroup,
          batchSize = testMaxBatchSize
        )
      } thenReturn {
        Future.successful(
          Vector(
            event1,
            event2,
            event3,
          )
        )
      }

      when {
        eventDeliveryServiceMock.deliverEvent(
          eventId = event1._1,
          eventContent = event1._2
        )
      } thenReturn Future.successful()

      when {
        eventDeliveryServiceMock.deliverEvent(
          eventId = event2._1,
          eventContent = event2._2
        )
      } thenReturn Future.failed(new RuntimeException("Kaboom"))


      val eventIdCaptor = ArgumentCaptor.forClass(classOf[Long])

      when {
        eventRepositoryMock.markGroupEventsAsSuccessful(
          eventGroup = ArgumentMatchers.eq(testEventGroup),
          untilEventId = eventIdCaptor.capture()
        )
      } thenReturn Future.successful()

      When("trying to sync the test group")
      val actual = tested.syncEventGroup(testEventGroup).futureValue

      Then("we attempt to deliver each event and we return that there are no more unsent events in the event group")
      actual shouldBe true
      verify(eventDeliveryServiceMock).deliverEvent(eventId = event1._1, eventContent = event1._2)
      verify(eventDeliveryServiceMock).deliverEvent(eventId = event2._1, eventContent = event2._2)
      verify(eventDeliveryServiceMock, never()).deliverEvent(eventId = event3._1, eventContent = event3._2)

      val actualEventIdMarked = eventIdCaptor.getValue
      actualEventIdMarked should be > event1._1
      actualEventIdMarked should be < event2._1
    }

    "return true (not all elements in group delivered) when batch size equals the max size and delivered successfully" in {

      Given("an event group containing a number of events smaller than the max batch size that is delivered successfully")
      val testEventGroup = "test-group"

      val event1 = (1L, "{1}")
      val event2 = (3L, "{2}")
      val event3 = (5L, "{3}")
      val event4 = (7L, "{4}")
      val event5 = (9L, "{5}")

      when {
        eventRepositoryMock.readUnsentFor(
          eventGroup = testEventGroup,
          batchSize = testMaxBatchSize
        )
      } thenReturn {
        Future.successful(
          Vector(
            event1,
            event2,
            event3,
            event4,
            event5,
          )
        )
      }

      when {
        eventRepositoryMock.markGroupEventsAsSuccessful(
          eventGroup = testEventGroup,
          untilEventId = event5._1
        )
      } thenReturn {
        Future.successful()
      }

      when {
        eventDeliveryServiceMock.deliverEvent(
          eventId = anyLong(),
          eventContent = anyString()
        )(any[ExecutionContext])
      } thenReturn Future.successful()

      When("trying to sync the test group")
      val actual = tested.syncEventGroup(testEventGroup).futureValue

      Then("we attempt to deliver each event and we return that there are no more unsent events in the event group")
      actual shouldBe true
      verify(eventDeliveryServiceMock).deliverEvent(eventId = event1._1, eventContent = event1._2)
      verify(eventDeliveryServiceMock).deliverEvent(eventId = event2._1, eventContent = event2._2)
      verify(eventDeliveryServiceMock).deliverEvent(eventId = event3._1, eventContent = event3._2)
      verify(eventDeliveryServiceMock).deliverEvent(eventId = event4._1, eventContent = event4._2)
      verify(eventDeliveryServiceMock).deliverEvent(eventId = event5._1, eventContent = event5._2)
      verify(eventRepositoryMock).markGroupEventsAsSuccessful(
        eventGroup = testEventGroup,
        untilEventId = event5._1
      )
    }
  }
}
