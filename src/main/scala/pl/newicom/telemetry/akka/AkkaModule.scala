package pl.newicom.telemetry.akka

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import zio.{Has, TaskLayer, ZLayer}

object AkkaModule {
  type AkkaModule = Has[ActorSystem]

  val live: TaskLayer[AkkaModule] =
    ZLayer.succeed(ActorSystem("telemetry", ConfigFactory.load()))

  trait Service {
    def actorSystem: ActorSystem
  }
}
