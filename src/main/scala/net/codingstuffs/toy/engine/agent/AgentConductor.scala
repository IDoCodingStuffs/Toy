package net.codingstuffs.toy.engine.agent

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import akka.util.Timeout
import net.codingstuffs.toy.engine.App.system
import net.codingstuffs.toy.engine.agent.Agent.{AgentParams, Declare}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

object AgentConductor {
  def props: Props = Props[AgentConductor]

  def props(members: Seq[Int],
    dataDumpGenerator: ActorRef): Props = Props(new AgentConductor(members, dataDumpGenerator))

  case class DataPoint(
    declare                   : Declare,
    params                    : AgentParams)

  case class GroupDataPoint(
    groupId: String,
    phenomeClusterCenter: Array[Int],
    distancePerMember: Array[Array[Int]])
}

class AgentConductor(members      : Seq[Int], dataAggregator: ActorRef)
  extends Actor with ActorLogging {

  import AgentConductor._

  // implicit ExecutionContext should be in scope
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = Duration.create(5, "seconds")

  val groupId: String = self.path.name
  val group: ActorSelection = system.actorSelection(s"/user/$groupId@@@*")

  var memberExpressions: Map[Int, String] = Map()

  def receive: PartialFunction[Any, Unit] = {
    case DataPoint(Declare(decision), params: AgentParams) =>

      val memberName = sender().path.name.split("@@@")(1)

      memberExpressions += (memberName.toInt -> decision)
      dataAggregator ! AgentParams(
        decision,
        params.group,
        params.turnInGroup,
        params.groupMembers,
        params.groupWeights,
        params.knownGroupPatterns,
        params.maslowianParams
      )


      system.actorSelection(s"/user/$groupId@@@${memberName.toInt + 1}*") ! Declare

      if (memberExpressions.size == members.size) {
        val phenomesNumerized = memberExpressions.values
          .map(phenome => phenome.toCharArray.map(_.toInt))

        val emptyCentroid = phenomesNumerized.head.map(_ => 0)
        val centroid = phenomesNumerized.foldLeft(emptyCentroid) {
          case (acc, item) => acc.zip(item).map(i => i._1 + i._2)
        }
          .map(item => item / phenomesNumerized.size)

        val distancePerMember = phenomesNumerized
          .map(phenome => centroid.zip(phenome).map(i => math.abs(i._1 - i._2)))

        dataAggregator ! GroupDataPoint(
          groupId,
          centroid,
          distancePerMember.toArray
        )
        //!TODO: Make cleanup more graceful
        context.stop(self)
      }
  }
}
