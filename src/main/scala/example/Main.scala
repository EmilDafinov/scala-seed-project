package example

import org.apache.pekko.http.scaladsl.Http

import scala.util.{Failure, Success}

object Main extends ApplicationRootModule {



  def main(args: Array[String]): Unit = {
    Http()
      .newServerAt("0.0.0.0", 9000)
      .bind(routes)
      .andThen {
        case Success(binding) =>
          scribe.info(s"Server started at ${binding.localAddress}")
        case Failure(exception) =>
          scribe.error("Failed starting http server, shitting down aplication", exception)
          System.exit(1)
      }
  }
}
