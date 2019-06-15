package net.codingstuffs.abilene.model

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import net.codingstuffs.abilene.generators.DataDumpGenerator.ActorDataPoint
import net.codingstuffs.abilene.model.Abilene.system
import net.codingstuffs.abilene.model.Member.{Declare, MemberParams}

object Group {
  def props: Props = Props[Group]

  def props(members: Set[String], dataDumpGenerator: ActorRef): Props = Props(new Group(members, dataDumpGenerator))

  case class DataPoint(declare: Declare, memberParams: MemberParams)

}

class Group(members: Set[String], dataDumpGenerator: ActorRef) extends Actor with ActorLogging {

  import Group._

  log.isWarningEnabled

  //!TODO: More fine tuned selections
  val group: ActorSelection = system.actorSelection("/user/*")
  val groupId = System.nanoTime() % Math.pow(10, 8)

  def receive: PartialFunction[Any, Unit] = {
    case DataPoint(Declare(decision), MemberParams((0,0), memberWeights, assumedOrKnownPreferences)) =>
      log.info(s"Decision received (from ${sender().path.name}): $decision")
      dataDumpGenerator ! ActorDataPoint(groupId, sender().path.name.split("---")(1), memberWeights, assumedOrKnownPreferences, decision)
  }
}
