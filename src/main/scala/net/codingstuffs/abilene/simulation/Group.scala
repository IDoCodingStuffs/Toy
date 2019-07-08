package net.codingstuffs.abilene.simulation

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import akka.util.Timeout
import net.codingstuffs.abilene.analytics.DataAggregatorActor.{ActorDataPoint, ActorRawDataPoint, CreateDump}
import net.codingstuffs.abilene.simulation.Abilene.system
import net.codingstuffs.abilene.simulation.Member.Declare
import net.codingstuffs.abilene.simulation.agent.AgentParamGenerator.DecisionParams

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

object Group {
  def props: Props = Props[Group]

  def props(members: Seq[Int],
    dataDumpGenerator: ActorRef): Props = Props(new Group(members, dataDumpGenerator))

  case class DataPoint(declare: Declare, memberParams: DecisionParams)

  case class GroupDataPoint(id: String, acceptance: Int, decision: Boolean)

}

class Group(members: Seq[Int], dataAggregator: ActorRef) extends Actor with ActorLogging {

  import Group._
  import akka.pattern.ask

  // implicit ExecutionContext should be in scope
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = Duration.create(5, "seconds")

  val groupId: String = self.path.name
  val group: ActorSelection = system.actorSelection(s"/user/$groupId@@@*")

  var memberDecisions: Map[Int, Boolean] = Map()

  def receive: PartialFunction[Any, Unit] = {
    case DataPoint(Declare(decision), params: DecisionParams) =>

      val memberName = sender().path.name.split("@@@")(1)
      memberDecisions += (memberName.toInt -> decision)

      dataAggregator !
        ActorDataPoint(groupId, memberName, params.selfParams._2, params.selfParams._3,
          params.groupPreferences.values.toSeq, params.groupWeights.values.toSeq, decision)
      dataAggregator !
        ActorRawDataPoint(groupId, memberName, params, decision)

        system.actorSelection(s"/user/$groupId@@@${memberName.toInt + 1}*") ! Declare

      if (memberName.toInt == members.size) dataAggregator !
          //!TODO: Refactor out of actor
          //Most voted decision wins system
          GroupDataPoint(groupId,
            memberDecisions.values.map(decision => if (decision) 1 else 0).sum,
            memberDecisions.values.groupBy(identity).maxBy(_._2.size)._1)
  }
}
