package net.codingstuffs.abilene.model

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

object Member {
  def props(member_id: String, preference: Double): Props = Props(new Member(member_id, preference))

  final case class DeclareDecision(id: String, decision: Boolean)

}

class Member(member_id: String, preference: Double) extends Actor with ActorLogging with ActorSystem {

  import Member._

  val decision_threshold = 0.5
  var member_preferences: Map[String, Double]
  var member_weights: Map[String, Double]

  def calculate_decision: Boolean = {
    val group_pref = member_weights.keySet.map(member => member_weights(member) * member_preferences(member)).sum
    val group_size = member_weights.keySet.size

    (group_pref + preference) / (group_size + 1) > decision_threshold
  }

  override def Receive: Receive = {
    case message: DeclareDecision =>
      if (message.decision) member_preferences(message.id) = 0 else member_preferences(message.id) = 1
    case _ =>  DeclareDecision(member_id, calculate_decision)
  }
}
