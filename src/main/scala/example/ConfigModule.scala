package example

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigModule {
  val conf: Config = ConfigFactory.load()
}
