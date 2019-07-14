package net.codingstuffs.toy.engine.iteration

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import net.codingstuffs.toy.engine.agent.Agent.{AgentParams, Declare}
import net.codingstuffs.toy.engine.agent.{Agent, AgentConductor}
import net.codingstuffs.toy.engine.intake.parse.ConfigUtil
import net.codingstuffs.toy.engine.iteration.IterationInstantiator.Generate
import net.codingstuffs.toy.engine.App
import net.codingstuffs.toy.engine.agent.AgentConductor.GroupDataPoint
import net.codingstuffs.toy.engine.analytics.AnalyticsGenerationActor
import net.codingstuffs.toy.engine.iteration.DataAggregatorActor.DataAggregate
import net.codingstuffs.toy.engine.iteration.behavior.GroupDynamics

import scala.util.Random

object IterationInstantiator {
  def props(
    system: ActorSystem): Props =
    Props(new IterationInstantiator(system))


  case class Generate(
    actorSystem: ActorSystem,
    aggregators: List[ActorRef],
    groupSet   : Set[Long],
    members    : Map[Long, Set[AgentParams]]
  )

}

class IterationInstantiator(
  system: ActorSystem,
) extends Actor with ActorLogging with GroupDynamics{
  private val agentDataPoints: Seq[AgentParams] = Seq()
  private val groupDataPoints: Seq[GroupDataPoint] = Seq()

  private val aggregatesReceived = 0

  override def receive: Receive = onMessage(agentDataPoints, groupDataPoints, aggregatesReceived)

  def reInit(genParams: Generate): Unit = {
    import genParams.{actorSystem, groupSet, members}
    groupSet.foreach(groupId => {

      val group = actorSystem.actorOf(AgentConductor.props(
        members(groupId).map(item => item.turnInGroup).toSeq.sorted,
        App.dataAggregators((groupId % ConfigUtil.AGGREGATOR_COUNT).toInt)),
        s"$groupId")

      var memberAgents: List[ActorRef] = List()

      members(groupId).foreach(param => {
        memberAgents = memberAgents :+ actorSystem.actorOf(
          Agent.props(group, param, new Random()),
          s"$groupId@@@${param.turnInGroup}")
      })
      //Ask first agent to declare
      actorSystem.actorSelection(s"/user/$groupId@@@1*") ! Declare
    })
  }

  private def onMessage(
    agentDataPoints: Seq[Agent.AgentParams],
    groupDataPoints: Seq[GroupDataPoint],
    aggregatesReceived: Int): Receive = {
    case receipt: DataAggregate =>
      context.become(
        onMessage(agentDataPoints ++ receipt.actorDataPoints,
          groupDataPoints ++ receipt.groupDataPoints,
          aggregatesReceived + 1))

      if (aggregatesReceived <= ConfigFactory.load.getInt("iterations")) {
        self ! Generate(
          system,
          App.dataAggregators,
          receipt.actorDataPoints.map(param => param.group.toLong).toSet,
          receipt.actorDataPoints.groupBy(_.group.toLong).map(item => item._1 -> item._2.toSet)
        )}
      else {
        App.analytics ! receipt
        App.analytics ! AnalyticsGenerationActor.Generate
        context.stop(self)
      }

    case generate: Generate => {
      context.become(
        onMessage(
          updateWeights(agentDataPoints, groupDataPoints),
          groupDataPoints,
          aggregatesReceived
      ))
      reInit(generate)
    }
  }
}
