package net.codingstuffs.abilene.model

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.BroadcastGroup
import net.codingstuffs.abilene.model.Abilene.system
import net.codingstuffs.abilene.model.Member.{Declare, DeclareDecision}

object Group {
  def props: Props = Props[Group]
  def props(members: Set[String]): Props = Props(new Group(members))
}

class Group(members: Set[String]) extends Actor with ActorLogging {
  import Group._

//  val router: ActorRef =
//    system.actorOf(BroadcastGroup(members.map(member => s"/user/$member")).props(), "groupRouter")

  //!TODO: More fine tuned decisions
  val group = system.actorSelection("/user/*")

  def receive: PartialFunction[Any, Unit] = {
    case Declare(decision) =>
      log.info(s"Decision received (from ${sender().path.name}): $decision")
      group ! DeclareDecision(sender().path.name, decision)
  }
}
