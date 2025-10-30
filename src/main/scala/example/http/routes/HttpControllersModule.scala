package example.http.routes

import com.typesafe.config.{Config, ConfigFactory}
import example.AkkaDependenciesModule
import example.shortener.{HashGenerator, RedisUrlResolver, ShortUrlRepository, UrlShortenerService}
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import redis.clients.jedis.{Jedis, JedisPooled}
import slick.jdbc.JdbcBackend.Database

import scala.util.Random

trait HttpControllersModule {
  this: AkkaDependenciesModule =>

  lazy val db = Database.forConfig(
    path = "postgres.db"
  )
  val config = ConfigFactory.load()

  val redisConfig = config.getConfig("redis")

  private val repository = new ShortUrlRepository(db)

  private val jedis = new JedisPooled(redisConfig.getString("url"), 6379)

  private val redisResolver = new RedisUrlResolver(jedis)
  private val service = new UrlShortenerService(repository, redisResolver, new HashGenerator(new Random()))

  val routes: Route =
    HealthController() ~ UrlShortenerController(service)
}
