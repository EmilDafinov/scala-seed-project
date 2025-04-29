package example

import org.apache.pekko.actor.ActorSystem

import scala.concurrent.ExecutionContext

trait AkkaDependenciesModule {

  implicit lazy val system: ActorSystem = ActorSystem("my-system")
  implicit lazy val ec: ExecutionContext = system.dispatcher
}
