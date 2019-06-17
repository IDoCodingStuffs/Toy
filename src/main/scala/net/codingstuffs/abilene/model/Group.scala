package net.codingstuffs.abilene.model

import akka.actor.{Actor, ActorLogging, Props}
import net.codingstuffs.abilene.model.Abilene.system
import net.codingstuffs.abilene.model.Member.{Declare, DeclareDecision}

object Group {
  def props: Props = Props[Group]

  def props(members: Set[String]): Props = Props(new Group(members))
}

class Group(members: Set[String]) extends Actor with ActorLogging {

  val group = system.actorSelection("/user/*")

  def receive: PartialFunction[Any, Unit] = {
    case Declare(decision) =>
      log.info(s"Decision received (from ${sender().path.name}): $decision")
      group ! DeclareDecision(sender().path.name, decision)
  }
}
