package net.codingstuffs.abilene.simulation

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import net.codingstuffs.abilene.intake.parse.ConfigUtil._
import net.codingstuffs.abilene.simulation.Group.DataPoint
import net.codingstuffs.abilene.simulation.agent._
import net.codingstuffs.abilene.simulation.agent.AgentParamGenerator.DecisionParams
import net.codingstuffs.abilene.simulation.agent.genetics.AgentGeneticsGenerator
import net.codingstuffs.abilene.simulation.agent.maslowian.MaslowianParamGenerator
import net.codingstuffs.abilene.simulation.calculators.DecisionCalculator
import net.codingstuffs.abilene.simulation.environment.AgentWorld
import net.codingstuffs.abilene.simulation.generators.random.FoldedGaussian

import scala.util.Random

object Member {
  def props(group: ActorRef,
    behaviorModel: AgentBehaviorModel,
    decisionModel: DecisionMakingModel,
    groupIndices : Set[Int],
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
  private val agentGenes = AgentGeneticsGenerator.get
  private val agentWorld = AgentWorld.get
  private val agentParamGenerator: AgentParamGenerator = new AgentParamGenerator(behaviorModel,
    randomGenerators, groupIndices)
  agentParamGenerator.self = name

  private val initialParams: DecisionParams = agentParamGenerator.get
  private val knownPreferences = initialParams.groupPreferences

  private val geneticsEpimorphism = AgentGeneticsGenerator.GENE_SET(agentGenes)

  private val adjustedParams: DecisionParams = {
    val adjustedForSelf = DecisionParams(
      (initialParams.selfParams._1, initialParams.selfParams._2,
        //Multiply self weight by sum of epimorphism
        if(agentWorld.contains(geneticsEpimorphism._1))
          geneticsEpimorphism._2 * initialParams.selfParams._3
        else 0),
      initialParams.groupPreferences,
      initialParams.groupWeights
    )
    val adjustedForGroup = behaviorModel match {

      case SimpleAgent => adjustedForSelf

      case MaslowianAgent =>
        //!TODO : Move this to member param generation
        val maslowianParams = new MaslowianParamGenerator(MASLOWIAN_MEAN_SD.map(
          mapping => FoldedGaussian.GENERATOR(mapping._2._1, mapping._2._2).nextDouble
        ).toList)

        DecisionParams(
          (adjustedForSelf.selfParams._1,
            //Homeostatic entropy calculated as inverse of a Maslowian sum
            adjustedForSelf.selfParams._2,
            (1 / maslowianParams.getMaslowianSum(name)) * adjustedForSelf.selfParams._3),

          adjustedForSelf.groupPreferences, adjustedForSelf.groupWeights)
    }

    adjustedForGroup
  }

  override def receive: Receive = onMessage(knownPreferences)

  private def onMessage(knownPreferences: Map[Int, Double]): Receive = {
    case message: ReceiveDecision =>
      if (message.decision) context.become(onMessage(knownPreferences + (message.member -> 1)))
      else context.become(onMessage(knownPreferences + (message.member -> 0)))
    case Declare =>
      val param = DecisionParams(adjustedParams.selfParams, knownPreferences, adjustedParams
        .groupWeights)
      val calc = new DecisionCalculator(param)
      group ! DataPoint(Declare(calc.get(decisionModel)), param)
  }
}
