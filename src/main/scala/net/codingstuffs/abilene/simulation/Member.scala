package net.codingstuffs.abilene.simulation

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.typesafe.config.ConfigFactory
import net.codingstuffs.abilene.intake.parse.ConfigUtil._
import net.codingstuffs.abilene.simulation.Group.DataPoint
import net.codingstuffs.abilene.simulation.agent._
import net.codingstuffs.abilene.simulation.agent.AgentParamGenerator.ExpressionParams
import net.codingstuffs.abilene.simulation.agent.genetics.calculators.{IterationBehavior, Mutations}
import net.codingstuffs.abilene.simulation.agent.maslowian.MaslowianParamGenerator
import net.codingstuffs.abilene.simulation.environment.AgentWorld
import net.codingstuffs.abilene.simulation.generators.random.FoldedGaussian

import scala.util.Random

object Member {
  def props(group  : ActorRef,
    behaviorModel  : AgentBehaviorModel,
    groupIndices   : Set[Int],
    randomGenerator: (Random, Random)): Props =
    Props(new Member(group, behaviorModel, groupIndices, randomGenerator))

  final case class ReceiveDecision(member: Int,
    expression: String)

  final case class Declare(expression: String)

}

class Member(group: ActorRef,
  behaviorModel   : AgentBehaviorModel,
  groupIndices    : Set[Int],
  randomGenerators: (Random, Random))
  extends Actor with ActorLogging {

  import Member._

  private val config = ConfigFactory.load

  private val name = self.path.name.split("@@@")(1)
  private val agentParamGenerator: AgentParamGenerator =
    new AgentParamGenerator(behaviorModel, randomGenerators, groupIndices, group.path.name)
  agentParamGenerator.self = name

  private val initialParams: ExpressionParams = agentParamGenerator.get
  private val initialGenome = initialParams.selfParams._2
  private val mutatedGenome = Mutations.mutate(initialGenome)

  private val agentWorld = AgentWorld.get

  private val knownExpressions = initialParams.groupExpressions

  private val maslowianParams = MASLOWIAN_MEAN_SD.map(
    mapping => FoldedGaussian.GENERATOR(mapping._2._1, mapping._2._2).nextDouble
  ).toList

  private val adjustedParams: ExpressionParams = {
    val adjustedForSelf = ExpressionParams(
      (initialParams.selfParams._1, mutatedGenome,
        //!TODO: Refactor into its own method in a util
        //!TODO: Introduce a scoring system or something instead of constant fitness on first match
        if (mutatedGenome.map(
          c => agentWorld.contains(c.toString)).foldLeft(false)(_ || _))
          initialParams.selfParams._3
        else config.getDouble("agent.genome.base_utility")),

      initialParams.groupExpressions,
      initialParams.groupWeights
    )

    behaviorModel match {

      case SimpleAgent => adjustedForSelf

      case MaslowianAgent =>
        //!TODO : Move this to member param generation
        val maslowianGenerator = new MaslowianParamGenerator(maslowianParams)

        ExpressionParams(
          (adjustedForSelf.selfParams._1, adjustedForSelf.selfParams._2, adjustedForSelf
            .selfParams._3),
          adjustedForSelf.groupExpressions,
          adjustedForSelf.groupWeights.map(weight =>
            weight._1 -> (1 / maslowianGenerator.getMaslowianSum(name)) * weight._2))
    }
  }

  override def receive: Receive = onMessage(knownExpressions)

  private def onMessage(knownPreferences: Map[Int, String]): Receive = {
    case message: ReceiveDecision =>
      context.become(onMessage(knownPreferences + (message.member -> message.expression)))
    case Declare =>
      val param = ExpressionParams(adjustedParams.selfParams, knownPreferences, adjustedParams
        .groupWeights)
      val state = (initialGenome, agentWorld, maslowianParams)
      group ! DataPoint(
        Declare(IterationBehavior
          .pickMutatedSelfOrCrossover(mutatedGenome, param)),
        param, state)
  }
}
