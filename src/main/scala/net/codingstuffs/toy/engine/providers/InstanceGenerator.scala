package net.codingstuffs.toy.engine.providers

import akka.actor.{ActorRef, ActorSystem}
import net.codingstuffs.toy.engine.agent.{Agent, AgentConductor}
import net.codingstuffs.toy.engine.agent.Agent.Declare
import net.codingstuffs.toy.engine.intake.parse.ConfigUtil
import net.codingstuffs.toy.engine.providers.InstanceGenerator.GenerationParams

import scala.util.Random


object InstanceGenerator {

  case class GenerationParams(
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
    val groupSize = ConfigUtil.GROUP_MIN +
      random.nextInt(ConfigUtil.GROUP_MAX - ConfigUtil.GROUP_MIN)

    var memberAgents: List[ActorRef] = List()

    val group = system.actorOf(AgentConductor.props(1.to(groupSize).toList,
      aggregators((groupId % ConfigUtil.AGGREGATOR_COUNT).toInt)),
      s"$groupId")
    val groupMembers = 1.to(groupSize).toSet
    groupMembers.foreach(index => {
      val generator = new AgentParamGenerator(groupId.toString, groupMembers, index.toString,
        (new Random(random.nextLong()), new Random(random.nextLong())), random.nextLong())
      memberAgents = memberAgents :+ system.actorOf(
        Agent.props(
          group, groupMembers, generator
        ),
        s"$groupId@@@$index")
    }
    )
    //Ask first agent to declare
    system.actorSelection(s"/user/$groupId@@@1*") ! Declare
  }

  def initIteration(): Unit = {
    1.to(ConfigUtil.EXTRA_ITERATIONS).foreach(_ => initSingleGroupInstance(dataAggregators))
  }
}
