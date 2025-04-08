package example

import example.http.routes.PingRoute
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}



object Hello extends App {
  implicit val system = ActorSystem("my-system")
  Http()
    .newServerAt("0.0.0.0", 8080)
    .bind(PingRoute())
    .andThen {
      case Success(binding) =>
        scribe.info(s"Server started at ${binding.localAddress}")
      case Failure(exception) =>
        scribe.error("Failed starting http server", exception)
        System.exit(1)
    }
}
