package net.codingstuffs.abilene.model

import java.time.Duration

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import net.codingstuffs.abilene.analytics.DataAggregatorActor
import net.codingstuffs.abilene.analytics.DataAggregatorActor.CreateDump

import scala.util.Random
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration
import scala.concurrent.duration.{Duration, FiniteDuration}

object Abilene extends App {

  import Member._


  val extraIterations: Int = if (args.length == 0) 1 else args(0).toInt

  val system: ActorSystem = ActorSystem("Abilene0")
  val dataDumpGenerator = system.actorOf(DataAggregatorActor.props, "dataDumper")

  var group, father, mother, wife, husband: ActorRef = _
  val groupMembers = Set("father", "mother", "wife", "husband")

  val random = new Random

  try {
    1.to(extraIterations).foreach(_ => {
      var groupId = math.abs(random.nextLong)
      implicit val timeout: Timeout = Timeout(FiniteDuration.apply(5, "seconds"))

      group = system.actorOf(Group.props(groupMembers, dataDumpGenerator), s"$groupId---group")

      father = system.actorOf(Member.props(group), s"$groupId@@@father")
      mother = system.actorOf(Member.props(group), s"$groupId@@@mother")
      wife = system.actorOf(Member.props(group), s"$groupId@@@wife")
      husband = system.actorOf(Member.props(group), s"$groupId@@@husband")

      father ! Declare
    })
  }
  finally {
    Thread.sleep(120000)
    dataDumpGenerator ! CreateDump
  }
}