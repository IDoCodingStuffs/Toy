package net.codingstuffs.abilene.model

import akka.actor.{ActorRef, ActorSystem}

object Abilene extends App {

  import Member._

  val system: ActorSystem = ActorSystem("Abilene0")

  try {
    val group: ActorRef = system.actorOf(GroupTracker.props, "group")

    val father = system.actorOf(
      Member.props(Map("father" -> 0.5, "mother" -> 0.5, "wife" -> 1.5, "husband" -> 1.5),
        Map("father" -> 0.1, "mother" -> 0.5, "wife" -> 0.8, "husband" -> 0.8)
      ), "father")

    val mother = system.actorOf(
      Member.props(Map("father" -> 0.5, "mother" -> 0.5, "wife" -> 1.5, "husband" -> 1.5),
        Map("father" -> 0.3, "mother" -> 0.1, "wife" -> 0.6, "husband" -> 0.6)
      ), "mother")

    val wife = system.actorOf(
      Member.props(Map("father" -> 1.5, "mother" -> 1, "wife" -> 0.75, "husband" -> 0.75),
        Map("father" -> 0.3, "mother" -> 0.3, "wife" -> 0.1, "husband" -> 0.4)
      ), "wife")

    val husband = system.actorOf(
      Member.props(Map("father" -> 1, "mother" -> 1, "wife" -> 1, "husband" -> 1),
        Map("father" -> 0.3, "mother" -> 0.3, "wife" -> 0.6, "husband" -> 0.1)
      ), "husband")

    father ! Declare

    wife ! DeclareDecision("father", true)
    wife ! Declare

    husband ! DeclareDecision("father", true)
    husband ! DeclareDecision("wife", true)
    husband ! Declare

    mother ! DeclareDecision("father", true)
    mother ! DeclareDecision("wife", true)
    mother ! DeclareDecision("husband", true)
    mother ! Declare

  } finally {
    system.terminate()
  }
}