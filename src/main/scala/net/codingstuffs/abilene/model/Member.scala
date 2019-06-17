package net.codingstuffs.abilene.model

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import net.codingstuffs.abilene.model.Member.MemberParams


object Member {
  def props(group: ActorRef, params: MemberParams): Props =
    Props(new Member(group, params))

  final case class DeclareDecision(member: String, decision: Boolean)

  final case class Declare(decision: Boolean)

  case class MemberParams(member_weights: Map[String, Double], assumedPreferences: Map[String, Double])

}

class Member(group: ActorRef, params: MemberParams)
  extends Actor {

  import Member._

  val log = Logging(context.system, this)

  val decision_threshold = 0.5

  var assumed_preferences: Map[String, Double] = params.assumedPreferences
  var member_weights: Map[String, Double] = params.member_weights

  log.info(s"Initial group preferences for ${self.path.name}: $assumed_preferences")
  log.info(s"Initial group weights for ${self.path.name}: $member_weights")

  def fuzzy_decision: Double = assumed_preferences.keySet.map(
    member => member_weights(member) * assumed_preferences(member)).sum / assumed_preferences.size

  def binary_decision: Boolean =
    fuzzy_decision > decision_threshold

  override def receive: Receive = {
    case message: DeclareDecision =>
      if (message.decision) assumed_preferences += (message.member -> 1) else assumed_preferences += (message.member -> 0)
      log.info(s"Decision received, updated preference map for ${self.path.name} : $assumed_preferences")
    case Declare =>
      log.info(s"Self-preference for ${self.path.name}: ${assumed_preferences(self.path.name)}")
      log.info(s"Decision fuzzy value for ${self.path.name}: $fuzzy_decision")
      group ! Declare(binary_decision)
  }
}
