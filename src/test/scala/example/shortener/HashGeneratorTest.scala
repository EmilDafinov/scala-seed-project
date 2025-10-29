package example.shortener

import org.scalatest.wordspec.AnyWordSpec

import java.net.URL
import scala.util.Random

class HashGeneratorTest extends AnyWordSpec {

  val tested = new HashGenerator(new Random())

  "HashGenerator" should {
    "return proper hash" in {
      //given
      val testUrl = new URL("http://example.com")

      //when
      tested.generateHash(testUrl)

      //then
      val a = 5
    }
  }
}
