package net.codingstuffs.abilene.simulation

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import akka.util.Timeout
import net.codingstuffs.abilene.analytics.DataAggregatorActor.{ActorDataPoint, ActorRawDataPoint}
import net.codingstuffs.abilene.simulation.Abilene.system
import net.codingstuffs.abilene.simulation.Member.Declare
import net.codingstuffs.abilene.simulation.agent.AgentParamGenerator.ExpressionParams

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

object Group {
  def props: Props = Props[Group]

  def props(members: Seq[Int],
    dataDumpGenerator: ActorRef): Props = Props(new Group(members, dataDumpGenerator))

  case class DataPoint(
    declare                   : Declare,
    memberParams              : ExpressionParams,
    state                     : (String, Set[String], List[Double]))

  case class GroupDataPoint(
    groupId: String,
    phenomeClusterCenter: Array[Int],
    averageDistance: Int,
    distancePerMember: Array[Array[Int]])

}

class Group(members: Seq[Int], dataAggregator: ActorRef) extends Actor with ActorLogging {

  import Group._

  // implicit ExecutionContext should be in scope
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = Duration.create(5, "seconds")

  val groupId: String = self.path.name
  val group: ActorSelection = system.actorSelection(s"/user/$groupId@@@*")

  var memberExpressions: Map[Int, String] = Map()

  def receive: PartialFunction[Any, Unit] = {
    case DataPoint(Declare(decision), params: ExpressionParams, state: (String, Set[String],
      List[Double])) =>

    val memberName = sender().path.name.split("@@@")(1)
    memberExpressions += (memberName.toInt -> decision)

    dataAggregator !
      ActorDataPoint(groupId, memberName, state._1, state._2, state._3)
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

      val averageDistance = phenomesNumerized
        .map(phenome => centroid.zip(phenome).map(i => math.abs(i._1 - i._2)).sum)
        .map(item => item / (phenomesNumerized.size * emptyCentroid.length)).sum

      val distancePerMember = phenomesNumerized
        .map(phenome => centroid.zip(phenome).map(i => math.abs(i._1 - i._2)))

      dataAggregator ! GroupDataPoint(
        groupId,
        centroid,
        averageDistance,
        distancePerMember.toArray
      )
      //!TODO: Make cleanup more graceful
      context.stop(self)
    }
  }
}
