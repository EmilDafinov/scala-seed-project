package example.shortener

import redis.clients.jedis.JedisPooled

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class RedisUrlResolver(jedis: JedisPooled)(implicit ec: ExecutionContext) {

  def resolve(shortUrl: String): Future[Option[URL]] = Future {
    Option(jedis.get(shortUrl))
      .map(new URL(_))
  }
  .recover {
    case NonFatal(_) => None
  }

  def insert(shortUrl: String, fullUrl: URL): Future[Unit] = Future {
    jedis.set(shortUrl, fullUrl.toString)
  }
}
