package net.codingstuffs.abilene.model

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging

object Member {
  def props(preference: Double, member_weights: Map[String, Double]): Props = Props(new Member(preference, member_weights))

  final case class DeclareDecision(member: String, decision: Boolean)

  case object Declare

}

class Member(preference: Double, member_weights: Map[String, Double]) extends Actor {

  import Member._
  val log = Logging(context.system, this)

  val decision_threshold = 0.5
  val default_assumed = 0.5

  var member_preferences: Map[String, Double] = member_weights.keys.map(key => key -> default_assumed).toMap

  def calculate_decision: Boolean = {
    val group_pref = member_weights.keySet.map(member => member_weights(member) * member_preferences(member)).sum
    (group_pref + preference) / member_weights.keySet.size > decision_threshold
  }

  override def receive: Receive = {
    case message: DeclareDecision =>
      if (message.decision) member_preferences += (message.member -> 1) else member_preferences += (message.member -> 0)
    case Declare => log.info(calculate_decision.toString)
  }
}
