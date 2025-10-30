package example.shortener

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, GivenWhenThen}
import org.scalatestplus.mockito.MockitoSugar

trait UnitTestSpec extends AnyWordSpec
  with ScalaFutures
  with BeforeAndAfterEach
  with BeforeAndAfterAll
  with GivenWhenThen
  with Matchers
  with MockitoSugar
