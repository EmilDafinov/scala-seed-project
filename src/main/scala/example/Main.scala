package example

import example.InputListeners.startHttpServer
import example.events.EventsModule
import example.http.routes.HttpControllersModule

object Main
  extends AkkaDependenciesModule
     with EventsModule
     with HttpControllersModule {

  def main(args: Array[String]): Unit = {

    startHttpServer
  }
}
