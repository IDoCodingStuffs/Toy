package net.codingstuffs.abilene.model

import akka.actor.{Actor, ActorRef, Props}
import net.codingstuffs.abilene.model.Group.DataPoint
import net.codingstuffs.abilene.model.decision_making.Models.{DecisionMakingModel, SimpleRoundup}
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

  implicit val decisionModel: DecisionMakingModel = SimpleRoundup
  private val name = self.path.name.split("---")(1)
  //!TODO: Make this specifiable
  private val agentParamGenerator: AgentParamGenerator = new AgentParamGenerator(Beta.GENERATOR)

  agentParamGenerator.self = name
  //!TODO: Generalize this
  agentParamGenerator.memberNames = GroupParamGenerator.AbileneMembers

  implicit var params: DecisionParams = agentParamGenerator.get

  private var knownPreferences = params.groupPreferences

  override def receive: Receive = {
    case message: ReceiveDecision =>
      if (message.decision)
        knownPreferences += (message.member -> 1)
      else knownPreferences += (message.member -> 0)

    case Declare =>
      group ! DataPoint(Declare(DecisionCalculator.get), params)
  }
}
