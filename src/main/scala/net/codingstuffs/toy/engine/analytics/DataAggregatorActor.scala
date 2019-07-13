package net.codingstuffs.toy.engine.analytics

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import net.codingstuffs.toy.engine.agent.AgentConductor.GroupDataPoint
import net.codingstuffs.toy.engine.analytics.DataAggregatorActor.{ActorDataPoint, ActorRawDataPoint, DataAggregate}
import net.codingstuffs.toy.engine.providers.AgentParamGenerator.ExpressionParams

object DataAggregatorActor {
  def props(
    analyzer: ActorRef,
    assigned: Int): Props =
    Props(new DataAggregatorActor(analyzer, assigned))

  case class ActorRawDataPoint(groupId: String,
    memberName: String,
    attunementDecisionParams: ExpressionParams,
    memberExpression: String)

  case class ActorDataPoint(
    groupId: String,
    memberName: String,
    phenome: String,
    maslowian: Map[String, Double]
  )

  case class DataAggregate(
    actorDataPoints: Seq[ActorDataPoint],
    actorRawDataPoints: Seq[ActorRawDataPoint],
    groupDataPoints: Seq[GroupDataPoint]
  )

}

class DataAggregatorActor(analytics: ActorRef, assigned: Int) extends Actor with ActorLogging {
  var actorDataPoints: Seq[ActorDataPoint] = Seq()
  var actorRawDataPoints: Seq[ActorRawDataPoint] = Seq()
  var groupDataPoints: Seq[GroupDataPoint] = Seq()

  override def receive: Receive = {
    case dataPoint: ActorDataPoint =>
      actorDataPoints = actorDataPoints :+ dataPoint
    case dataPoint: ActorRawDataPoint =>
      actorRawDataPoints = actorRawDataPoints :+ ActorRawDataPoint(dataPoint.groupId, dataPoint
        .memberName, dataPoint.attunementDecisionParams, dataPoint.memberExpression.mkString
        .concat(""))
    case dataPoint: GroupDataPoint =>
      groupDataPoints = groupDataPoints :+ dataPoint
      if (groupDataPoints.size == assigned) {
        analytics ! DataAggregate(actorDataPoints, actorRawDataPoints, groupDataPoints)
      }
  }
}
