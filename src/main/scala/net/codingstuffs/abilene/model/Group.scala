package net.codingstuffs.abilene.model

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import net.codingstuffs.abilene.analytics.DataAggregatorActor.ActorDataPoint
import net.codingstuffs.abilene.model.Abilene.system
import net.codingstuffs.abilene.model.Member.Declare
import net.codingstuffs.abilene.model.decision_making.generators.AgentParamGenerator.DecisionParams

object Group {
  def props: Props = Props[Group]

  def props(members: Set[String], dataDumpGenerator: ActorRef): Props = Props(new Group(members, dataDumpGenerator))

  case class DataPoint(declare: Declare, memberParams: DecisionParams)

}

class Group(members: Set[String], dataDumpGenerator: ActorRef) extends Actor with ActorLogging {

  import Group._

  log.isWarningEnabled

  //!TODO: More fine tuned selections
  val group: ActorSelection = system.actorSelection("/user/*")
  val groupId = System.nanoTime() % Math.pow(10, 8)

  def receive: PartialFunction[Any, Unit] = {
    case DataPoint(Declare(decision), params: DecisionParams) =>
      dataDumpGenerator !
        ActorDataPoint(groupId, params, decision)
  }
}
