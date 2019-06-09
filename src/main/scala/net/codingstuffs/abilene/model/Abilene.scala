package net.codingstuffs.abilene.model

import akka.actor.{ActorRef, ActorSystem}
import net.codingstuffs.abilene.generators.MemberParamGenerator._

object Abilene extends App {

  import Member._

  val system: ActorSystem = ActorSystem("Abilene0")

  try {
    val groupMembers = Set("father", "mother", "wife", "husband")
    val group: ActorRef = system.actorOf(Group.props(groupMembers), "group")

    val father = system.actorOf(Member.props(group, generate(groupMembers)), "father")
    val mother = system.actorOf(Member.props(group, generate(groupMembers)), "mother")
    val wife = system.actorOf(Member.props(group, generate(groupMembers)), "wife")
    val husband = system.actorOf(Member.props(group, generate(groupMembers)), "husband")

    father ! Declare
    wife ! Declare
    husband ! Declare
    mother ! Declare

  } finally {
    Thread.sleep(1000)
    system.terminate()
  }
}