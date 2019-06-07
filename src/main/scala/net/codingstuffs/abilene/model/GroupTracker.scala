package net.codingstuffs.abilene.model

import akka.actor.{Actor, ActorLogging, Props}

object GroupTracker {
  def props: Props = Props[GroupTracker]
  final case class DeclareDecision(decision: Boolean)
}

class GroupTracker extends Actor with ActorLogging {
  import GroupTracker._
  def receive: PartialFunction[Any, Unit] = {
    case DeclareDecision(decision) =>
      log.info(s"Decision received (from ${sender()}): $decision")
  }
}
