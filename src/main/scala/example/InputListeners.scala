package example

import example.Main.routes
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object InputListeners {

  def startHttpServer(implicit system: ActorSystem, ec: ExecutionContext) = {
    Http()
      .newServerAt("0.0.0.0", 9000)
      .bind(routes)
      .andThen {
        case Success(binding) =>
          scribe.info(s"Server started at ${binding.localAddress}")
        case Failure(exception) =>
          scribe.error("Failed starting http server, shutting down application", exception)
          System.exit(1)
      }
  }
}
