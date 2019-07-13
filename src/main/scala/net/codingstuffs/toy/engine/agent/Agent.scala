package net.codingstuffs.toy.engine.agent

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import net.codingstuffs.toy.engine.agent.Agent.AgentParams
import net.codingstuffs.toy.engine.agent.AgentConductor.DataPoint
import net.codingstuffs.toy.engine.phenetics.calculators.IterationBehavior

import scala.util.Random

object Agent {
  def props(conductor: ActorRef, params: AgentParams, random: Random) =
    Props(new Agent(conductor, params, random))

  final case class AgentParams(
    phenome: String,
    group: String,
    turnInGroup: Int,
    groupMembers: Set[Int],
    groupWeights: Map[Int, Double],
    knownGroupPatterns: Map[Int, String],
    maslowianParams: List[Double]
  )

  final case class ReceiveDecision(member: Int,
    expression: String)

  final case class Declare(expression: String)

}

class Agent(conductor: ActorRef, params: AgentParams, random: Random)
  extends Actor with ActorLogging {

  import Agent._
  import params._

  override def receive: Receive = onMessage(knownGroupPatterns)

  private def onMessage(knownGroupPatterns: Map[Int, String]): Receive = {
    case message: ReceiveDecision =>
      context.become(onMessage(knownGroupPatterns + (message.member -> message.expression)))
    case Declare =>
      conductor ! DataPoint(
        Declare(IterationBehavior
          .pickMutatedSelfOrAttune(params, new Random(random.nextLong))), params)
      //!TODO: Make cleanup more graceful
      context.stop(self)
  }
}
