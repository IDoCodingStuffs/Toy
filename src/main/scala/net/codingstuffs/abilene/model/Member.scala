package net.codingstuffs.abilene.model

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import net.codingstuffs.abilene.model.Group.DataPoint
import net.codingstuffs.abilene.model.Member.MemberParams


object Member {
  def props(group: ActorRef, params: MemberParams): Props =
    Props(new Member(group, params))

  final case class DeclareDecision(member: String, decision: Boolean)
  final case class Declare(decision: Boolean)

  case class MemberParams(memberWeights: Map[String, Double], assumedOrKnownPreferences: Map[String, Double])

}

class Member(group: ActorRef, params: MemberParams)
  extends Actor {

  import Member._

  val log = Logging(context.system, this)

  val decision_threshold = 0.5

  var assumed_preferences: Map[String, Double] = params.assumedOrKnownPreferences
  var member_weights: Map[String, Double] = params.memberWeights

  log.info(s"Initial member preferences for $self: $assumed_preferences")
  log.info(s"Initial member weights for $self: $member_weights")

  def calculate_decision: Boolean =
    assumed_preferences.keySet.map(member => member_weights(member) * assumed_preferences(member)).sum / assumed_preferences.size > decision_threshold

  override def receive: Receive = {
    case message: DeclareDecision =>
      if (message.decision) assumed_preferences += (message.member -> 1) else assumed_preferences += (message.member -> 0)
      log.debug(s"Decision received (from ${sender()}), updated preference map for $self : $assumed_preferences")
    case Declare =>
      log.debug(s"Decision fuzzy value for $self: $calculate_decision")
      group ! DataPoint(Declare(calculate_decision), MemberParams(member_weights, assumed_preferences))
  }
}
