package net.codingstuffs.abilene.model

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import net.codingstuffs.abilene.model.Group.DataPoint
import net.codingstuffs.abilene.model.decision_making.models.Models._
import net.codingstuffs.abilene.model.decision_making.calculators.DecisionCalculator
import net.codingstuffs.abilene.model.decision_making.generators.{AgentParamGenerator, GroupParamGenerator}
import net.codingstuffs.abilene.model.decision_making.generators.AgentParamGenerator.DecisionParams
import net.codingstuffs.abilene.model.decision_making.generators.random.{Beta, FoldedGaussian, Discrete, Uniform}
import net.codingstuffs.abilene.model.decision_making.models.ArithmeticRoundup.WeightedRoundup

object Member {
  def props(group: ActorRef): Props =
    Props(new Member(group))

  final case class ReceiveDecision(member: String, decision: Boolean)

  final case class Declare(decision: Boolean)

}

class Member(group: ActorRef)
  extends Actor with ActorLogging{

  import Member._

  implicit val decisionModel: DecisionMakingModel = WeightedRoundup(0.9999, 0.0001)

  private val name = self.path.name.split("@@@")(1)
  //!TODO: Make this specifiable
  private val agentParamGenerator: AgentParamGenerator = new AgentParamGenerator(Uniform.GENERATOR)

  agentParamGenerator.self = name
  //!TODO: Generalize this
  agentParamGenerator.memberNames = GroupParamGenerator.AbileneMembers

  implicit var params: DecisionParams = agentParamGenerator.get

  private val knownPreferences = params.groupPreferences

  override def receive: Receive = onMessage(knownPreferences)

  private def onMessage(knownPreferences: Map[String, Double]): Receive = {
    case message: ReceiveDecision =>
      if (message.decision) context.become(onMessage(knownPreferences + (message.member -> 1))) else context.become(onMessage(knownPreferences + (message.member -> 0)))
    case Declare =>
      val param = DecisionParams(params.selfParams, knownPreferences, params.groupWeights)
      val calc = new DecisionCalculator(param)
      group ! DataPoint(Declare(calc.get), param)
  }
}
