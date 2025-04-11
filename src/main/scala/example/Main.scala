package example

import example.http.routes.HealthController
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Main {

  implicit val system = ActorSystem("my-system")

  def main(args: Array[String]): Unit = {
    Http()
      .newServerAt("0.0.0.0", 9000)
      .bind(HealthController())
      .andThen {
        case Success(binding) =>
          scribe.info(s"Server started at ${binding.localAddress}")
        case Failure(exception) =>
          scribe.error("Failed starting http server, shitting down aplication", exception)
          System.exit(1)
      }
  }
}
