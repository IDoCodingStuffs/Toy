package net.codingstuffs.toy.iteration.agent

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import akka.util.Timeout
import net.codingstuffs.toy.analytics.DataAggregatorActor.{ActorDataPoint, ActorRawDataPoint}
import net.codingstuffs.toy.iteration.App.system
import net.codingstuffs.toy.iteration.agent.Agent.Declare
import net.codingstuffs.toy.iteration.agent.providers.AgentParamGenerator.ExpressionParams

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

object ConductorActor {
  def props: Props = Props[ConductorActor]

  def props(members: Seq[Int],
    dataDumpGenerator: ActorRef): Props = Props(new ConductorActor(members, dataDumpGenerator))

  case class DataPoint(
    declare                   : Declare,
    memberParams              : ExpressionParams,
    state                     : (String, Map[String, Double]))

  case class GroupDataPoint(
    groupId: String,
    phenomeClusterCenter: Array[Int],
    distancePerMember: Array[Array[Int]])

}

class ConductorActor(members: Seq[Int], dataAggregator: ActorRef) extends Actor with ActorLogging {

  import ConductorActor._

  // implicit ExecutionContext should be in scope
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = Duration.create(5, "seconds")

  val groupId: String = self.path.name
  val group: ActorSelection = system.actorSelection(s"/user/$groupId@@@*")

  var memberExpressions: Map[Int, String] = Map()

  def receive: PartialFunction[Any, Unit] = {
    case DataPoint(Declare(decision), params: ExpressionParams, state: (String,
      Map[String, Double])) =>

    val memberName = sender().path.name.split("@@@")(1)
    memberExpressions += (memberName.toInt -> decision)

    dataAggregator !
      ActorDataPoint(groupId, memberName, state._1, state._2)
    dataAggregator !
      ActorRawDataPoint(groupId, memberName, params, decision)

    system.actorSelection(s"/user/$groupId@@@${memberName.toInt + 1}*") ! Declare

    if (memberExpressions.size == members.size) {
      val phenomesNumerized = memberExpressions.values
        .map(phenome => phenome.toCharArray.map(_.toInt))

      val emptyCentroid = phenomesNumerized.head.map(_=>0)
      val centroid = phenomesNumerized.foldLeft(emptyCentroid){
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
