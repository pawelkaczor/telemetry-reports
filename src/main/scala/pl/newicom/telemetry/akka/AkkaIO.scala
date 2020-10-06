package pl.newicom.telemetry.akka

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import zio.{Has, TaskLayer, ZLayer}

object AkkaIO {
  type AkkaIO = Has[ActorSystem]

  val live: TaskLayer[AkkaIO] =
    ZLayer.succeed(ActorSystem("telemetry", ConfigFactory.load()))

  trait Service {
    def actorSystem: ActorSystem
  }
}
