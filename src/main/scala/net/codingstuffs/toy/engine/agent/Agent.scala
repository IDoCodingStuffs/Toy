package net.codingstuffs.toy.engine.agent

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.typesafe.config.ConfigFactory
import net.codingstuffs.toy.engine.agent.AgentConductor.DataPoint
import net.codingstuffs.toy.engine.intake.parse.ConfigUtil
import net.codingstuffs.toy.engine.phenetics.calculators.{IterationBehavior, Mutations}
import net.codingstuffs.toy.engine.phenetics.AgentPheneticsGenerator
import net.codingstuffs.toy.engine.providers.{AgentParamGenerator, MaslowianParamGenerator}
import net.codingstuffs.toy.engine.providers.AgentParamGenerator.ExpressionParams
import net.codingstuffs.toy.engine.providers.random_generators.FoldedGaussian

import scala.util.Random

object Agent {
  def props(group: ActorRef,
    groupIndices: Set[Int],
    params: AgentParamGenerator): Props =
    Props(new Agent(group, groupIndices, params))

  final case class ReceiveDecision(member: Int,
    expression: String)

  final case class Declare(expression: String)

}

class Agent(
  group: ActorRef,
  groupIndices: Set[Int],
  generator: AgentParamGenerator)
  extends Actor with ActorLogging {

  import Agent._

  private val name = self.path.name.split("@@@")(1)
  private val params = generator.adjustedParams
  private val knownExpressions = params.groupExpressions

  override def receive: Receive = onMessage(knownExpressions)

  private def onMessage(knownGroupPatterns: Map[Int, String]): Receive = {
    case message: ReceiveDecision =>
      context.become(onMessage(knownGroupPatterns + (message.member -> message.expression)))
    case Declare =>
      val param = ExpressionParams(params.selfParams, knownGroupPatterns, params
        .groupWeights)
      val state = (generator.initialPhenome, generator.maslowianParams)
      group ! DataPoint(
        Declare(IterationBehavior.pickMutatedSelfOrAttune(
          generator.mutatedPhenome,
          generator.initialPhenome,
          param,
          new Random(generator.preferenceGenerator.nextLong))),
        param,
        state)
      //!TODO: Make cleanup more graceful
      context.stop(self)
  }
}
