package net.codingstuffs.abilene.model

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import net.codingstuffs.abilene.analytics.DataAggregatorActor
import net.codingstuffs.abilene.analytics.DataAggregatorActor.CreateDump

import scala.util.Random

object Abilene extends App {

  import Member._


  val extraIterations: Int = if (args.length == 0) 1 else args(0).toInt

  val system: ActorSystem = ActorSystem("Abilene0")
  val dataDumpGenerator = system.actorOf(DataAggregatorActor.props, "dataDumper")

  var group, father, mother, wife, husband: ActorRef = _
  val groupMembers = Set("father", "mother", "wife", "husband")

  try {
    1.to(extraIterations).foreach(_ => {
      var uniqueTime = System.nanoTime()
      val random = new Random

      group = system.actorOf(Group.props(groupMembers, dataDumpGenerator), s"${math.abs(random.nextLong)}---group")

      father = system.actorOf(Member.props(group), s"$uniqueTime---father")
      mother = system.actorOf(Member.props(group), s"$uniqueTime---mother")
      wife = system.actorOf(Member.props(group), s"$uniqueTime---wife")
      husband = system.actorOf(Member.props(group), s"$uniqueTime---husband")

      father ! Declare
      Thread.sleep(10)
      wife ! Declare
      Thread.sleep(10)
      husband ! Declare
      Thread.sleep(10)
      mother ! Declare

    })
  }
  finally {
    Thread.sleep(10000)
    dataDumpGenerator ! CreateDump
  }
}