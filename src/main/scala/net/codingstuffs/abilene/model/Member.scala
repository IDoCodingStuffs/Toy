package net.codingstuffs.abilene.model

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object Member {
  def props(preference: Double, groupSize: Double, groupRef: ActorRef): Props = Props(new Member(preference, groupSize, groupRef))

  final case class DeclareDecision(decision: Boolean)

  case object Declare

}

class Member(preference: Double, groupSize: Double, groupRef: ActorRef) extends Actor {

  import Member._

  val decision_threshold = 0.5
  val default_assumed = 0.5

  var member_preferences: Map[ActorRef, Double] = Map()
  var member_weights: Map[ActorRef, Double] = Map()

  def calculate_decision: Boolean = {
    val group_pref = member_weights.keySet.map(member => member_weights(member) * member_preferences(member)).sum + default_assumed * (groupSize - member_weights.keySet.size - 1)
    (group_pref + preference) / groupSize > decision_threshold
  }

  override def receive: Receive = {
    case message: DeclareDecision =>
      if (message.decision) member_preferences += (sender() -> 1) else member_preferences += (sender() -> 0)
    case _ => groupRef ! DeclareDecision(calculate_decision)
  }
}
