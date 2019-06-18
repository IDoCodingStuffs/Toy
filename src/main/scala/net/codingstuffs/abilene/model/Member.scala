package net.codingstuffs.abilene.model

import akka.actor.{Actor, ActorRef, Props}
import net.codingstuffs.abilene.model.Group.DataPoint
import net.codingstuffs.abilene.model.decision_making.Models.{DecisionMakingModel, NaiveRoundup, SimpleSociotropyAutonomy, WeightedSociotropyAutonomy}
import net.codingstuffs.abilene.model.decision_making.calculators.DecisionCalculator
import net.codingstuffs.abilene.model.decision_making.generators.{AgentParamGenerator, GroupParamGenerator}
import net.codingstuffs.abilene.model.decision_making.generators.AgentParamGenerator.DecisionParams
import net.codingstuffs.abilene.model.decision_making.generators.random.{Beta, Static, Uniform}

object Member {
  def props(group: ActorRef): Props =
    Props(new Member(group))

  final case class ReceiveDecision(member: String, decision: Boolean)

  final case class Declare(decision: Boolean)

}

class Member(group: ActorRef)
  extends Actor {

  import Member._

  implicit val decisionModel: DecisionMakingModel = SimpleSociotropyAutonomy(0.01, 0.99)

  private val name = self.path.name.split("---")(1)
  //!TODO: Make this specifiable
  private val agentParamGenerator: AgentParamGenerator = new AgentParamGenerator(Beta.GENERATOR)

  agentParamGenerator.self = name
  //!TODO: Generalize this
  agentParamGenerator.memberNames = GroupParamGenerator.AbileneMembers

  implicit var params: DecisionParams = agentParamGenerator.get

  private val knownPreferences = params.groupPreferences

  override def receive: Receive = onMessage(knownPreferences)

  private def onMessage(knownPreferences: Map[String, Double]): Receive = {
    case message: ReceiveDecision =>
      if (message.decision)
        context.become(onMessage(knownPreferences + (message.member -> 1)))
      else context.become(onMessage(knownPreferences + (message.member -> 0)))

    case Declare =>
      group ! DataPoint(Declare(DecisionCalculator.get), params)
  }
}
