package net.codingstuffs.abilene.model

import akka.actor.{ActorRef, ActorSystem}

object Abilene extends App {
  import Member._

  // Create the 'helloAkka' actor system
  val system: ActorSystem = ActorSystem("Abilene0")

  try {
    val group: ActorRef = system.actorOf(GroupTracker.props, "group")

    val father: ActorRef =
      system.actorOf(Member.props(0.1, 4, group), "father")
    val wife: ActorRef =
      system.actorOf(Member .props(0.2, 4, group), "wife")
    val husband: ActorRef =
      system.actorOf(Member.props(0.1, 4, group), "husband")
    val mother: ActorRef =
      system.actorOf(Member.props(0.2, 4, group), "mother")

    father.

    father ! Declare
    wife ! Declare
    husband ! Declare
    mother ! Declare

  } finally {
    system.terminate()
  }
}