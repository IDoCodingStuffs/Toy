package net.codingstuffs.toy.iteration.agent

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.typesafe.config.ConfigFactory
import net.codingstuffs.toy.intake.parse.ConfigUtil._
import net.codingstuffs.toy.iteration.agent.providers.{AgentParamGenerator, MaslowianParamGenerator}
import net.codingstuffs.toy.iteration.agent.providers.AgentParamGenerator.ExpressionParams
import net.codingstuffs.toy.iteration.agent.ConductorActor.DataPoint
import net.codingstuffs.toy.iteration.generators.random.FoldedGaussian
import net.codingstuffs.toy.phenetics.calculators.{IterationBehavior, Mutations}
import net.codingstuffs.toy.phenetics.AgentPheneticsGenerator

import scala.util.Random

object Agent {
  def props(group: ActorRef,
    groupIndices : Set[Int],
    randomSeed   : Long): Props =
    Props(new Agent(group, groupIndices, randomSeed))

  final case class ReceiveDecision(member: Int,
    expression: String)

  final case class Declare(expression: String)

}

class Agent(group: ActorRef,
  groupIndices: Set[Int],
  randomSeed: Long)
  extends Actor with ActorLogging {

  import Agent._

  private val config = ConfigFactory.load
  private val random = new Random(randomSeed)

  private val name = self.path.name.split("@@@")(1)
  private val agentParamGenerator: AgentParamGenerator =
    new AgentParamGenerator(
      (new Random(random.nextLong), new Random(random.nextLong)),
      groupIndices, group.path.name)
  agentParamGenerator.self = name

  private val initialParams: ExpressionParams = agentParamGenerator.get
  private val initialPhenome = initialParams.selfParams._2
  private val mutatedPhenome = Mutations.mutate(initialPhenome, new Random(random.nextLong))

  private val knownExpressions = initialParams.groupExpressions

  private val maslowianParams = MASLOWIAN_MEAN_SD.map(
    mapping => mapping._1 -> FoldedGaussian.GENERATOR(mapping._2._1, mapping._2._2).nextDouble
  )

  private val adjustedParams: ExpressionParams = {
    val adjustedForSelf = ExpressionParams(
      (initialParams.selfParams._1, mutatedPhenome._1,
        //!TODO: Refactor into its own method in a util
        if (AgentPheneticsGenerator.GENE_SET.contains(mutatedPhenome._1))
          initialParams.selfParams._3 * AgentPheneticsGenerator.GENE_SET(mutatedPhenome._1)
        else config.getDouble("agent.phenome.base_utility")),

      initialParams.groupExpressions,
      initialParams.groupWeights
    )

    //!TODO : Move this to member param generation
    val maslowianGenerator = new MaslowianParamGenerator(maslowianParams)

    ExpressionParams(
      (adjustedForSelf.selfParams._1, adjustedForSelf.selfParams._2, adjustedForSelf
        .selfParams._3),
      adjustedForSelf.groupExpressions,
      adjustedForSelf.groupWeights.map(weight =>
        weight._1 -> (1 / maslowianGenerator.getMaslowianSum(name)) * weight._2))

  }

  override def receive: Receive = onMessage(knownExpressions)

  private def onMessage(knownPreferences: Map[Int, String]): Receive = {
    case message: ReceiveDecision =>
      context.become(onMessage(knownPreferences + (message.member -> message.expression)))
    case Declare =>
      val param = ExpressionParams(adjustedParams.selfParams, knownPreferences, adjustedParams
        .groupWeights)
      val state = (initialPhenome, maslowianParams)
      group ! DataPoint(
        Declare(IterationBehavior.pickMutatedSelfOrAttune(
          mutatedPhenome,
          initialPhenome,
          param,
          new Random(random.nextLong))),
        param,
        state)
  }
}
