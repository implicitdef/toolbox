package com.github.implicitdef.toolbox.throttle

import akka.actor.{Actor, ActorSystem, Props}
import akka.contrib.throttle.Throttler.{Rate, SetTarget}
import akka.contrib.throttle.TimerBasedThrottler
import akka.pattern.ask
import akka.util.Timeout
import com.github.implicitdef.toolbox.throttle.SimpleThrottler.BaseActor
import org.scalactic._

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success}

class SimpleThrottler[O]
  (rate: Rate, timeout: Timeout)
  (implicit classTag: ClassTag[() => Future[O]]) {

  private val actorSystem = ActorSystem()
  private implicit def e = actorSystem.dispatcher
  private implicit def t = timeout
  private val throttledActor = {
    val base = actorSystem.actorOf(Props(new BaseActor))
    val throttled = actorSystem.actorOf(Props(classOf[TimerBasedThrottler], rate))
    throttled ! SetTarget(Some(base))
    throttled
  }

  def execute(func: () => Future[O]): Future[O] = {
    throttledActor.?(func).mapTo[Or[O, Throwable]].map {
      case Good(output) => output
      case Bad(t) => throw t
    }
  }

  def shutdown = actorSystem.terminate()

}

object SimpleThrottler {

  private class BaseActor[O]
    (implicit classTag: ClassTag[() => Future[O]], e: ExecutionContext) extends Actor {
    def receive = {
      case classTag(func) =>
        val _sender = sender
        func().onComplete {
          case Success(output) =>
            _sender ! Good(output)
          case Failure(t) =>
            _sender ! Bad(t)
        }
    }
  }


}
