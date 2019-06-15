package net.codingstuffs.abilene.model

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import net.codingstuffs.abilene.generators.DataDumpGenerator
import net.codingstuffs.abilene.generators.DataDumpGenerator.CreateDump
import net.codingstuffs.abilene.generators.MemberParamGenerator._

object Abilene extends App {

  import Member._


  val extraIterations: Int = if (args.length == 0) 1 else args(0).toInt

  val system: ActorSystem = ActorSystem("Abilene0")
  val dataDumpGenerator = system.actorOf(DataDumpGenerator.props, "dataDumper")

  var group, father, mother, wife, husband: ActorRef = _
  val groupMembers = Set("father", "mother", "wife", "husband")

  try {
    1.to(extraIterations).foreach(_ => {
      var uniqueTime = System.nanoTime()

      group = system.actorOf(Group.props(groupMembers, dataDumpGenerator), s"$uniqueTime---group")

      father = system.actorOf(Member.props(group, generate(groupMembers)), s"$uniqueTime---father")
      mother = system.actorOf(Member.props(group, generate(groupMembers)), s"$uniqueTime---mother")
      wife = system.actorOf(Member.props(group, generate(groupMembers)), s"$uniqueTime---wife")
      husband = system.actorOf(Member.props(group, generate(groupMembers)), s"$uniqueTime---husband")

      father ! Declare
      wife ! Declare
      husband ! Declare
      mother ! Declare

      father ! PoisonPill
      wife ! PoisonPill
      husband ! PoisonPill
      mother ! PoisonPill
    })
  }
  finally {
    Thread.sleep(1000)
    dataDumpGenerator ! CreateDump
    dataDumpGenerator ! PoisonPill
  }
}