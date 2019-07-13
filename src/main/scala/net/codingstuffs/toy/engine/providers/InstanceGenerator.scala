package net.codingstuffs.toy.engine.providers

import akka.actor.{ActorRef, ActorSystem}
import net.codingstuffs.toy.engine.analytics.DataAggregatorActor
import net.codingstuffs.toy.engine.agent.{Agent, AgentConductor}
import net.codingstuffs.toy.engine.agent.Agent.Declare
import net.codingstuffs.toy.engine.providers.InstanceGenerator.GenerationParams
import org.springframework.stereotype.Component

import scala.util.Random


object InstanceGenerator {
  case class GenerationParams(
    aggregatorCount: Int,
    extraIterations: Int,
    groupMax: Int,
    groupMin: Int,
    random: Random,
    analytics: ActorRef,
    dataAggregators: List[ActorRef],
    system: ActorSystem
  )
}

class InstanceGenerator(params: GenerationParams) {
  import params._

  def initSingleGroupInstance(aggregators: List[ActorRef]): Unit = {
    val groupId = System.nanoTime()
    val groupSize = groupMin + random.nextInt(groupMax - groupMin)

    var memberAgents: List[ActorRef] = List()

    val group = system.actorOf(AgentConductor.props(1.to(groupSize).toList,
      aggregators((groupId % aggregatorCount).toInt)),
      s"$groupId")
    val groupMembers = 1.to(groupSize).toSet
    groupMembers.foreach(index => {
      memberAgents = memberAgents :+ system.actorOf(
        Agent.props(
          group, groupMembers, random.nextLong
        ),
        s"$groupId@@@$index")
    }
    )
    //Ask first agent to declare
    system.actorSelection(s"/user/$groupId@@@1*") ! Declare
  }

  def initIteration(): Unit = {
    1.to(extraIterations).foreach(_ => initSingleGroupInstance(dataAggregators))
  }
}
