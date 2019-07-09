package net.codingstuffs.abilene.simulation

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import net.codingstuffs.abilene.analytics.{AnalyticsGenerationActor, DataAggregatorActor}
import net.codingstuffs.abilene.intake.parse.ConfigUtil._
import net.codingstuffs.abilene.simulation.agent.{MaslowianAgent, SimpleAgent}
import org.joda.time.LocalDateTime

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object Abilene extends App {

  import Member._

  val config = ConfigFactory.load()
  System.setProperty("hadoop.home.dir", config.getString("hadoop.home.dir"))

  val extraIterations: Int = config.getInt("group.count")
  val groupMax = config.getInt("group.size.max")
  val groupMin = config.getInt("group.size.min")
  val aggregatorCount = config.getInt("data.aggregator.count")

  val random = new Random
  implicit val timeout: Timeout = Timeout(FiniteDuration.apply(5, "seconds"))

  val studyModel = config.getString("agent.behavior.model") match {
    case "Simplified" => SimpleAgent
    case "Maslowian" => MaslowianAgent
  }

  val system: ActorSystem = ActorSystem("Abilene")
  val analytics = system.actorOf(AnalyticsGenerationActor.props, "AnalyticsGenerator")
  var dataAggregators: List[ActorRef] = List()

  //Init data aggregators
  1.to(aggregatorCount).foreach(
    index => dataAggregators = dataAggregators :+
      system.actorOf(DataAggregatorActor.props(analytics, extraIterations / aggregatorCount),
        s"DataAggregator$index")
  )

  def initGroup(aggregators: List[ActorRef]): Unit = {
    val groupId = System.nanoTime()
    val groupSize = groupMin + random.nextInt(groupMax - groupMin)

    var memberAgents: List[ActorRef] = List()

    val group = system.actorOf(Group.props(1.to(groupSize).toList,
      aggregators((groupId % aggregatorCount).toInt)),
      s"$groupId")
    val groupMembers = 1.to(groupSize).toSet
    groupMembers.foreach(index => {
      memberAgents = memberAgents :+ system.actorOf(
        Member.props(
          group, studyModel, DECISION_MODEL, groupMembers,
          (PREFERENCE_GENERATOR, WEIGHTS_GENERATOR)
        ),
        s"$groupId@@@$index")
    }
    )
    //Ask first agent to declare
    system.actorSelection(s"/user/$groupId@@@1*") ! Declare
  }

  1.to(extraIterations).foreach(_ => initGroup(dataAggregators))
}