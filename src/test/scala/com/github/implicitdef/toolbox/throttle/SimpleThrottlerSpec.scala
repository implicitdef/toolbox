package com.github.implicitdef.toolbox.throttle

import akka.contrib.throttle.Throttler._
import com.github.implicitdef.toolbox.Pimp._
import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class SimpleThrottlerSpec extends FlatSpec with BeforeAndAfterEach {

  val brokenInput = "broken input"

  val throttler = new SimpleThrottler[(Duration, String)](
    rate = 3 msgsPer 2.seconds,
    timeout = 10.minutes
  )

  override def afterEach = {
    throttler.shutdown
  }

  it should "throttle properly" in {

    val start = System.nanoTime()

    def launchWaitAndMeasure(name: String, expectedLaunchTime: Duration) = {
      val (launchedAt, output) = throttler.execute(() => {
        val launchedAt = Duration.fromNanos(System.nanoTime() - start)
        Future {
          Thread.sleep(300)
          (launchedAt, name)
        }
      }).await()
      output === name
      Logger.info(s"$name -> ${launchedAt.toMillis}")
      launchedAt.toMillis should equal (expectedLaunchTime.toMillis +- 200)
    }

    launchWaitAndMeasure(s"A", 0.milliseconds)
    launchWaitAndMeasure(s"B", 300.milliseconds)
    launchWaitAndMeasure(s"C", 600.milliseconds)
    launchWaitAndMeasure(s"D", 2000.milliseconds)
    launchWaitAndMeasure(s"E", 2300.milliseconds)
    launchWaitAndMeasure(s"F", 2600.milliseconds)
    launchWaitAndMeasure(s"G", 4000.milliseconds)
    launchWaitAndMeasure(s"H", 4300.milliseconds)
  }

  it should "forward exceptions properly" in {
    an [IllegalArgumentException] should be thrownBy throttler.execute({
      throw new IllegalArgumentException
    }).await()
  }

}
