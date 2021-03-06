package com.github.implicitdef.toolbox

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.control.Exception._
import scala.concurrent.duration._

object Pimp extends Pimp
trait Pimp {

  def err(s: String) = throw new RuntimeException(s)

  implicit class RichOption[A](o: Option[A]){
    def orErr(msg: String): A =
      o.getOrElse(err(msg))
  }
  implicit class RichFuture[A](f: Future[A]){
    def await(atMost: Duration = 1.minute): A =
      Await.result(f, atMost)
    def thenSideEffect(block: A => Unit)(implicit e: ExecutionContext): Future[A] =
      f.map { v =>
        block(v)
        v
      }
  }
  implicit class RichSeqFutures[A](fs: Seq[Future[A]]){
    def sequence(implicit e: ExecutionContext): Future[Seq[A]] =
      Future.sequence(fs)(Seq.canBuildFrom, e)
  }

  def timed[A](block: => Future[A])(implicit e: ExecutionContext): Future[(A, Duration)] = {
    val start = System.nanoTime()
    block.map { a =>
      a -> Duration.fromNanos(System.nanoTime() - start)
    }
  }

  def swallowing[T](exceptions: Class[_]*)(block: => T): Option[T] =
    catching(exceptions:_*).opt(block)

}
