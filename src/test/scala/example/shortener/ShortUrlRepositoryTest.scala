package example.shortener

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.wordspec.AnyWordSpec
import slick.jdbc.JdbcBackend.Database

import java.net.URL
import scala.concurrent.ExecutionContext.Implicits.global

class ShortUrlRepositoryTest extends AnyWordSpec with ScalaFutures {
  private lazy val dbConfig = Database.forConfig("postgres.db")
  val tested = new ShortUrlRepository(dbConfig)

  "ShortUrlRepository" should {
    "fsfds" in {

      //when
      val url = new URL("https://medium.com/streamingdata/running-kafka-locally-inside-kubernetes-25e84586bbf3")
      val url2 = new URL("https://example.com")

      val eventualResult = for {
        firstInserted <- tested.tryInsert("uMmqfaO", url)
        duplicateInsert <- tested.tryInsert("uMmqfaO", url)
        collision <- tested.tryInsert("uMmqfaO", url2)
      } yield collision



      whenReady(eventualResult.failed) { actualResult =>
        actualResult.isInstanceOf[RuntimeException]

      }


    }
  }
}
