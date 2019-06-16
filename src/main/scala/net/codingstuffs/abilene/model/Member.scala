package net.codingstuffs.abilene.model

import akka.actor.{Actor, ActorRef, Props}
import net.codingstuffs.abilene.model.Group.DataPoint
import net.codingstuffs.abilene.model.Member.MemberParams
import net.codingstuffs.abilene.model.decision_making.DecisionMakingModels.{BetaDistributedRoundup, DecisionMakingModel, SimpleRoundup, SocialImpactNSL, SociotropyAutonomy}


object Member {
  def props(group: ActorRef, params: MemberParams): Props =
    Props(new Member(group, params))

  final case class ReceiveDecision(member: String, decision: Boolean)

  final case class Declare(decision: Boolean)

  case class MemberParams(decisionThreshold: Double,
                          memberWeights: Map[String, Double],
                          assumedOrKnownPreferences: Map[String, Double])

}

class Member(group: ActorRef, params: MemberParams)
  extends Actor {

  import Member._

  val decision_threshold: Double = params.decisionThreshold
  var assumed_preferences: Map[String, Double] = params.assumedOrKnownPreferences
  var member_weights: Map[String, Double] = params.memberWeights
  val decisionMakingModel: DecisionMakingModel = params.decisionMakingModel

  private val selfPreference = assumed_preferences(self.path.name.split("---")(1))

  def calculate_decision(model: DecisionMakingModel): Boolean = {
    model match {
      case SimpleRoundup => selfPreference >= 0.5
      case BetaDistributedRoundup(threshold) => selfPreference >= threshold
      case SociotropyAutonomy() =>
        assumed_preferences.keySet
          .map(member => assumed_preferences(member))
          .sum / assumed_preferences.size > decision_threshold

      case SocialImpactNSL =>
        assumed_preferences.keySet
          .map(member => member_weights(member) * assumed_preferences(member))
          .sum / assumed_preferences.size > decision_threshold
    }
  }

  override def receive: Receive = {
    case message: ReceiveDecision =>
      if (message.decision) assumed_preferences += (message.member -> 1) else assumed_preferences += (message.member -> 0)
    case Declare =>
      group ! DataPoint(Declare(calculate_decision), MemberParams(decision_threshold, member_weights, assumed_preferences))
  }
}
