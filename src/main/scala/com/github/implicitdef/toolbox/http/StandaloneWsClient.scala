package com.github.implicitdef.toolbox.http
import java.io.IOException

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.libs.ws.ahc.AhcWSClient

class StandaloneWsClient extends WSClient {

  private implicit val actorSystem = ActorSystem()
  private implicit val materializer = ActorMaterializer()
  private val ahcWsClient = AhcWSClient()

  override def underlying[T] = ahcWsClient.underlying[T]

  override def url(url: String): WSRequest = ahcWsClient.url(url)

  @scala.throws[IOException]
  override def close(): Unit = {
    ahcWsClient.close()
    materializer.shutdown()
    actorSystem.terminate()
  }
}
