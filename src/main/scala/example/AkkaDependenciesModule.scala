package example

import org.apache.pekko.actor.ActorSystem

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

trait AkkaDependenciesModule {

  implicit lazy val system: ActorSystem = ActorSystem("my-system")
  implicit lazy val ec: ExecutionContext = ExecutionContext.fromExecutor(
    Executors.newCachedThreadPool()
  )
}
