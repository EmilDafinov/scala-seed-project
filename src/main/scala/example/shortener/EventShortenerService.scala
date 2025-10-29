package example.shortener

import org.apache.pekko.NotUsed
import org.apache.pekko.actor.ActorSystem

import java.net.URL
import java.util.UUID
import java.util.zip.{CRC32, CRC32C}
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import scala.util.control.NonFatal
import org.apache.pekko.stream._
import org.apache.pekko.stream.scaladsl._
import org.checkerframework.framework.qual.Unused

class EventShortenerService(repository: ShortUrlRepository, hashGenerator: HashGenerator)(implicit ec: ExecutionContext, system: ActorSystem) {

//  @tailrec
//  final def retry[A](future: => Future[A], remainingAttempts: Int): Future[A] = {
//    if (remainingAttempts <= 0) future
//    else retry(
//      future = future.recoverWith {
//        case NonFatal(_) => future
//      },
//      remainingAttempts = remainingAttempts - 1
//    )
//  }

//  private def  retryUntilSuccess(operation: Int => Future[Boolean])(implicit mat: Materializer): Future[Boolean] = {
//    val g = Source.fromGraph(GraphDSL.create() { implicit builder =>
//      import GraphDSL.Implicits._
//      val in = Source.single(0)
//
//      val merge = builder.add(Merge[Int](2))
//      val partition = builder.add(
//        Partition[(Boolean, Int)](2, {
//          case (true, attemptNum: Int) => 0
//          case (false, attemptNum: Int) => 1
//        })
//      )
//
//      val workerFlow: Flow[Int, (Boolean, Int), NotUsed] = Flow[Int].mapAsync(1)(attemptNum =>
//        operation(attemptNum).map(success => success -> attemptNum)
//      )
//
//      val retryFlow: Flow[(Boolean, Int), Int, NotUsed] = Flow[(Boolean, Int)]
//        .map { case (_, attemptCount) => attemptCount + 1 }
//
//      in ~> merge ~> workerFlow ~> partition
//      partition.out(1) ~> retryFlow ~> merge
//
//      SourceShape.of(partition.out(0).map(_._1).outlet)
//    })
//
//    g.runFold(List.empty[Boolean]) { case (acc, bool: Boolean) =>
//      bool::acc
//    }.map(_.head)
//  }

  def retry[A](future: Int => Future[A], currentAttempt:Int, maxAttempts: Int): Future[A] =
    if (currentAttempt == maxAttempts) future(currentAttempt)
    else future(currentAttempt).recoverWith {
      case e => retry(future, currentAttempt + 1, maxAttempts)
    }

  private def shortenWithAttempt(longUrl: URL)(attemptNumber: Int): Future[Boolean] = {
    for {
      urlHash <- Future(hashGenerator.generateHash(longUrl, attemptNumber))
      insertedWithoutDuplication <- repository.tryInsert(urlHash, longUrl)
    } yield insertedWithoutDuplication
  }

  final def shorten(longUrl: URL): Future[Boolean] = {
    retry(shortenWithAttempt(longUrl), 0, 10)
  }



  //TODO: Remove this service method...
  def lookup(urlHash: String): Future[Option[URL]] =
    repository.resolveFullUrl(urlHash)

}
