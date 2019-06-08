package net.codingstuffs.abilene.model

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging

object Member {
  def props(member_weights: Map[String, Double], assumed_preferences: Map[String, Double]): Props =
    Props(new Member(member_weights, assumed_preferences))

  final case class DeclareDecision(member: String, decision: Boolean)

  case object Declare

}

class Member(member_weights: Map[String, Double], assumedPreferences: Map[String, Double])
  extends Actor {

  import Member._

  val log = Logging(context.system, this)

  val decision_threshold = 0.5
  val default_assumed = 0.5

  var assumed_preferences = assumedPreferences

  def calculate_decision: Boolean = {
    var sum = 0.0
    assumed_preferences.keySet.foreach(member => sum += (member_weights(member) * assumed_preferences(member)))
    sum / assumed_preferences.size > decision_threshold
  }

  override def receive: Receive = {
    case message: DeclareDecision =>
      if (message.decision) assumed_preferences += (message.member -> 1) else assumed_preferences += (message.member -> 0)
    case Declare => log.info(calculate_decision.toString)
  }
}
