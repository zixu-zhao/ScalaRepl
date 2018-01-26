import scala.concurrent.{Await, ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


val packet: Future[Array[Byte]] = Future(Array(0.toByte))

/** flatMap is the key. If we using the map instead of flatMap,
  * the return type would be S -> Array[Byte]. However, flatMap wraps
  * the return type into the Future, so flatMap would expect
  * the type of result as Future[S] -> Future[ Array[Byte] ]
  */
val confirmation: Future[Array[Byte]] = packet.flatMap(p => Future(p))


/** Use ready and result in Awaitable for demo or testing
  * They are really dangerous by block the code
  * Never use it in the asynchronous scenario
  */

abstract class Awaitable[T] extends AnyRef {
   def ready(atMost: Duration): Unit

   def result(atMost: Duration): T
}

/**
  * Await use case
  */
val c = Await.result(confirmation, Duration(100, "second")) // seconds
print(c.toString)

/** Customized Future trait
  */

trait FutureCustomized[T] extends Awaitable[T] {

  def filter(p: T => Boolean): FutureCustomized[T]

  def flatMap[S, U](f: T => FutureCustomized[S]): FutureCustomized[U]

  def map[S](f: T => S): FutureCustomized[S]

  def recoverWith(f: PartialFunction[Throwable, FutureCustomized[T]]): FutureCustomized[T]

  /** this fallbackTo provides an idea of if THIS failed, get
    * the result from THAT Future, and it THAT failed as well,
    * then return the error of THIS
    */
  def fallbackTo(that: => FutureCustomized[T]): FutureCustomized[T] = {
    this recoverWith {
      case _ => that recoverWith { case _ => this }
    }
  }

  /**
    * Another approach to deal with failure -> retry until the block succeed,
    * or return failure after a number of time tries.
    */
  def failed(exception: Exception): FutureCustomized[T] = ???

  def retry(noTimes: Int)(block: => FutureCustomized[T]): FutureCustomized[T] = {
    if (noTimes == 0) {
      this.failed(new Exception("Sorry")) // actually is Future.failed(new Exception("Sorry")
    } else {
      block fallbackTo { retry(noTimes - 1) { block } }
    }
  }

}


