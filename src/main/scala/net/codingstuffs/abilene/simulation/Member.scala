package net.codingstuffs.abilene.simulation

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import net.codingstuffs.abilene.simulation.Group.DataPoint
import net.codingstuffs.abilene.simulation.decision_making.calculators.DecisionCalculator
import net.codingstuffs.abilene.simulation.decision_making.generators.GroupParamGenerator
import net.codingstuffs.abilene.simulation.decision_making.models.AgentParamGenerator.DecisionParams
import net.codingstuffs.abilene.simulation.decision_making.models.{
  AgentBehaviorModel,
  AgentParamGenerator, DecisionMakingModel, MaslowianAgent, StochasticAgent
}
import net.codingstuffs.abilene.simulation.decision_making.models.maslowian.MaslowianParamGenerator

import scala.util.Random

object Member {
  def props(group: ActorRef,
    behaviorModel: AgentBehaviorModel,
    decisionModel: DecisionMakingModel,
    randomGenerator: (Random, Random)): Props =
    Props(new Member(group, behaviorModel, decisionModel, randomGenerator))

  final case class ReceiveDecision(member: String, decision: Boolean)

  final case class Declare(decision: Boolean)

}

class Member(group: ActorRef,
  behaviorModel   : AgentBehaviorModel,
  decisionModel   : DecisionMakingModel,
  randomGenerators: (Random, Random))
  extends Actor with ActorLogging {

  import Member._

  private val name = self.path.name.split("@@@")(1)
  private val agentParamGenerator: AgentParamGenerator = new AgentParamGenerator(behaviorModel,
    randomGenerators)

  agentParamGenerator.self = name
  agentParamGenerator.memberNames = GroupParamGenerator.AbileneMembers

  val initialParams = agentParamGenerator.get

  implicit var params: DecisionParams = behaviorModel match {

    case StochasticAgent => initialParams

    case MaslowianAgent => {
      val maslowianParams = MaslowianParamGenerator.instance
      DecisionParams(
        (initialParams.selfParams._1,
          initialParams.selfParams._2,
          (1 / maslowianParams.getMaslowianSum(name)) * initialParams.selfParams._3),
        initialParams.groupPreferences,
        initialParams.groupWeights)
    }
  }

  private val knownPreferences = params.groupPreferences

  override def receive: Receive = onMessage(knownPreferences)

  private def onMessage(knownPreferences: Map[String, Double]): Receive = {
    case message: ReceiveDecision =>
      if (message.decision) context.become(onMessage(knownPreferences + (message.member -> 1)))
      else context.become(onMessage(knownPreferences + (message.member -> 0)))
    case Declare =>
      val param = DecisionParams(params.selfParams, knownPreferences, params.groupWeights)
      val calc = new DecisionCalculator(param)
      group ! DataPoint(Declare(calc.get(decisionModel)), param)
  }
}
