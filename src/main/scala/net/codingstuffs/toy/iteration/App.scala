package net.codingstuffs.toy.iteration

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import net.codingstuffs.toy.analytics.{AnalyticsGenerationActor, DataAggregatorActor}
import net.codingstuffs.toy.iteration.agent.{Agent, ConductorActor}

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object App extends App {

  import Agent._

  val config = ConfigFactory.load()
  System.setProperty("hadoop.home.dir", config.getString("hadoop.home.dir"))

  val extraIterations: Int = config.getInt("group.count")
  val groupMax = config.getInt("group.size.max")
  val groupMin = config.getInt("group.size.min")
  val aggregatorCount = config.getInt("data.aggregator.count")

  private val random = new Random(config.getLong("generator.seed.main"))

  implicit val timeout: Timeout = Timeout(FiniteDuration.apply(5, "seconds"))

  val system: ActorSystem = ActorSystem("Abilene")
  val analytics = system.actorOf(AnalyticsGenerationActor.props, "AnalyticsGenerator")
  var dataAggregators: List[ActorRef] = List()

  def initAggregators(): Unit = 1.to(aggregatorCount).foreach(
    index => dataAggregators = dataAggregators :+
      system.actorOf(DataAggregatorActor.props(analytics, extraIterations / aggregatorCount),
        s"DataAggregator$index")
  )

  def initSingleGroupInstance(aggregators: List[ActorRef]): Unit = {
    val groupId = System.nanoTime()
    val groupSize = groupMin + random.nextInt(groupMax - groupMin)

    var memberAgents: List[ActorRef] = List()

    val group = system.actorOf(ConductorActor.props(1.to(groupSize).toList,
      aggregators((groupId % aggregatorCount).toInt)),
      s"$groupId")
    val groupMembers = 1.to(groupSize).toSet
    groupMembers.foreach(index => {
      memberAgents = memberAgents :+ system.actorOf(
        Agent.props(
          group, groupMembers, random.nextLong
        ),
        s"$groupId@@@$index")
    }
    )
    //Ask first agent to declare
    system.actorSelection(s"/user/$groupId@@@1*") ! Declare
  }

  def initIteration(): Unit = {
    initAggregators()
    1.to(extraIterations).foreach(_ => initSingleGroupInstance(dataAggregators))
  }

  initIteration()
}