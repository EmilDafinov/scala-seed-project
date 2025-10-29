package example

import example.InputListeners.startHttpServer
import example.http.routes.HttpControllersModule

object Main
  extends AkkaDependenciesModule
     with HttpControllersModule {

  def main(args: Array[String]): Unit = {

    startHttpServer
  }
}
