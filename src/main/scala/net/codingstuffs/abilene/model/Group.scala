package net.codingstuffs.abilene.model

import akka.actor.{Actor, ActorLogging, Props}
import net.codingstuffs.abilene.model.Member.{Declare, DeclareDecision}

object Group {
  def props: Props = Props[Group]
  def props(members: Set[String]): Props = Props(new Group(members))
}

class Group(members: Set[String]) extends Actor with ActorLogging {
  import Group._
  def receive: PartialFunction[Any, Unit] = {
    case Declare(decision) =>
      log.info(s"Decision received (from ${sender()}): $decision")
      members.foreach(member => context.actorSelection(s"../$member") ! DeclareDecision(sender().path.name, decision))
  }
}
