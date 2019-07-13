package net.codingstuffs.toy.engine.analytics

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import net.codingstuffs.toy.engine.agent.Agent.AgentParams
import net.codingstuffs.toy.engine.agent.AgentConductor.GroupDataPoint
import net.codingstuffs.toy.engine.analytics.DataAggregatorActor.DataAggregate

object DataAggregatorActor {
  def props(
    analyzer: ActorRef,
    assigned: Int): Props =
    Props(new DataAggregatorActor(analyzer, assigned))

  case class DataAggregate(
    actorDataPoints: Seq[AgentParams],
    groupDataPoints: Seq[GroupDataPoint]
  )

}

class DataAggregatorActor(analytics: ActorRef, assigned: Int) extends Actor with ActorLogging {
  var actorDataPoints: Seq[AgentParams] = Seq()
  var groupDataPoints: Seq[GroupDataPoint] = Seq()

  override def receive: Receive = {
    case dataPoint: AgentParams =>
      actorDataPoints = actorDataPoints :+ dataPoint
    case dataPoint: GroupDataPoint =>
      groupDataPoints = groupDataPoints :+ dataPoint
      if (groupDataPoints.size == assigned) {
        analytics ! DataAggregate(actorDataPoints, groupDataPoints)
        actorDataPoints = Seq()
        groupDataPoints = Seq()
      }
  }
}
