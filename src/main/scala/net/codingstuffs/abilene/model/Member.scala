package net.codingstuffs.abilene.model

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import net.codingstuffs.abilene.model.Group.DataPoint
import net.codingstuffs.abilene.model.Member.MemberParams
import net.codingstuffs.abilene.model.logic.DecisionMakingModels.{LogicModel, Selfish, SimpleConsensusSeeking, WeightedConsensusSeeking}


object Member {
  def props(group: ActorRef, params: MemberParams): Props =
    Props(new Member(group, params))

  final case class ReceiveDecision(member: String, decision: Boolean)

  final case class Declare(decision: Boolean)

  case class MemberParams(decisionThreshold: Double,
                          decisionMakingModel: LogicModel,
                          memberWeights: Map[String, Double],
                          assumedOrKnownPreferences: Map[String, Double])

}

class Member(group: ActorRef, params: MemberParams)
  extends Actor {

  import Member._

  val log = Logging(context.system, this)

  val decision_threshold: Double = params.decisionThreshold
  var assumed_preferences: Map[String, Double] = params.assumedOrKnownPreferences
  var member_weights: Map[String, Double] = params.memberWeights
  val decisionMakingModel: LogicModel = params.decisionMakingModel

  def calculate_decision(implicit model: LogicModel = decisionMakingModel): Boolean = {
    model match {
      case Selfish => assumed_preferences(self.path.name.split("---")(1)) >= decision_threshold

      case SimpleConsensusSeeking =>
        assumed_preferences.keySet
          .map(member => assumed_preferences(member))
          .sum / assumed_preferences.size > decision_threshold

      case WeightedConsensusSeeking =>
        assumed_preferences.keySet
          .map(member => member_weights(member) * assumed_preferences(member))
          .sum / assumed_preferences.size > decision_threshold
    }
  }

  override def receive: Receive = {
    case message: ReceiveDecision =>
      if (message.decision) assumed_preferences += (message.member -> 1) else assumed_preferences += (message.member -> 0)
    case Declare =>
      group ! DataPoint(Declare(calculate_decision), MemberParams(decision_threshold, decisionMakingModel, member_weights, assumed_preferences))
  }
}
