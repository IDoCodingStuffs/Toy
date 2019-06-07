package net.codingstuffs.abilene.model

import akka.actor.{ActorRef, ActorSystem}

object Abilene extends App {

  import Member._

  val system: ActorSystem = ActorSystem("Abilene0")

  try {
    val group: ActorRef = system.actorOf(GroupTracker.props, "group")

    val father = system.actorOf(Member.props(1, Map("father" -> 0.5, "mother" -> 1, "wife" -> 1, "husband" -> 1)), "father")
    val wife: ActorRef = system.actorOf(Member.props(1, Map("father" -> 1, "mother" -> 1, "wife" -> 1, "husband" -> 1)), "wife")
    val husband: ActorRef = system.actorOf(Member.props(1, Map("father" -> 1, "mother" -> 1, "wife" -> 1, "husband" -> 1)), "husband")
    val mother: ActorRef = system.actorOf(Member.props(1, Map("father" -> 1, "mother" -> 1, "wife" -> 1, "husband" -> 1)), "mother")

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