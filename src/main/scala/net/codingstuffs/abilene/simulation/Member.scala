package net.codingstuffs.abilene.simulation

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import net.codingstuffs.abilene.intake.parse.ConfigUtil._
import net.codingstuffs.abilene.simulation.Group.DataPoint
import net.codingstuffs.abilene.simulation.calculators.DecisionCalculator
import net.codingstuffs.abilene.simulation.generators.random.FoldedGaussian
import net.codingstuffs.abilene.simulation.agent.AgentParamGenerator.DecisionParams
import net.codingstuffs.abilene.simulation.agent.{
  AgentBehaviorModel,
  AgentParamGenerator, DecisionMakingModel, MaslowianAgent, SimpleAgent
}
import net.codingstuffs.abilene.simulation.agent.maslowian.MaslowianParamGenerator

import scala.util.Random

object Member {
  def props(group: ActorRef,
    behaviorModel  : AgentBehaviorModel,
    decisionModel  : DecisionMakingModel,
    groupIndices    : Set[Int],
    randomGenerator: (Random, Random)): Props =
    Props(new Member(group, behaviorModel, decisionModel, groupIndices, randomGenerator))

  final case class ReceiveDecision(member: Int, decision: Boolean)

  final case class Declare(decision: Boolean)

}

class Member(group: ActorRef,
  behaviorModel   : AgentBehaviorModel,
  decisionModel   : DecisionMakingModel,
  groupIndices    : Set[Int],
  randomGenerators: (Random, Random))
  extends Actor with ActorLogging {

  import Member._

  private val name = self.path.name.split("@@@")(1)
  private val agentParamGenerator: AgentParamGenerator = new AgentParamGenerator(behaviorModel,
    randomGenerators, groupIndices)

  agentParamGenerator.self = name

  val initialParams: DecisionParams = agentParamGenerator.get

  implicit var params: DecisionParams = behaviorModel match {

    case SimpleAgent => initialParams

    case MaslowianAgent =>
      //!TODO : Move this to member param generation
      val maslowianParams = new MaslowianParamGenerator(MASLOWIAN_MEAN_SD.map(
        mapping => FoldedGaussian.GENERATOR(mapping._2._1, mapping._2._2).nextDouble
      ).toList)

      DecisionParams(
        (initialParams.selfParams._1,
          //Homeostatic entropy calculated as inverse of a Maslowian sum
          initialParams.selfParams._2,
          (1 / maslowianParams.getMaslowianSum(name)) * initialParams.selfParams._3),

        initialParams.groupPreferences, initialParams.groupWeights)
  }


  private val knownPreferences = params.groupPreferences

  override def receive: Receive = onMessage(knownPreferences)

  private def onMessage(knownPreferences: Map[Int, Double]): Receive = {
    case message: ReceiveDecision =>
      if (message.decision) context.become(onMessage(knownPreferences + (message.member -> 1)))
      else context.become(onMessage(knownPreferences + (message.member -> 0)))
    case Declare =>
      val param = DecisionParams(params.selfParams, knownPreferences, params.groupWeights)
      val calc = new DecisionCalculator(param)
      group ! DataPoint(Declare(calc.get(decisionModel)), param)
  }
}
