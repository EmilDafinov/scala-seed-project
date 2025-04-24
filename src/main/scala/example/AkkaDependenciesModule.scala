package example

import org.apache.pekko.actor.ActorSystem

import scala.concurrent.ExecutionContext

trait AkkaDependenciesModule {

  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val ec: ExecutionContext = system.dispatcher
}
