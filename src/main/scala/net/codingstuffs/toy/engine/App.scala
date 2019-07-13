package net.codingstuffs.toy.engine

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import net.codingstuffs.toy.engine.analytics.{AnalyticsGenerationActor, DataAggregatorActor}
import net.codingstuffs.toy.engine.providers.InstanceGenerator
import net.codingstuffs.toy.engine.providers.InstanceGenerator.GenerationParams

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object App extends App {

  val config = ConfigFactory.load()
  System.setProperty("hadoop.home.dir", config.getString("hadoop.home.dir"))

  val extraIterations: Int = config.getInt("group.count")
  val groupMax = config.getInt("group.size.max")
  val groupMin = config.getInt("group.size.min")
  val aggregatorCount = config.getInt("data.aggregator.count")

  private val random = new Random(config.getLong("generator.seed.main"))
  implicit val timeout: Timeout = Timeout(FiniteDuration.apply(5, "seconds"))

  val system: ActorSystem = ActorSystem("Abilene")
  val analytics =
    system.actorOf(AnalyticsGenerationActor.props, "AnalyticsGenerator")
  val dataAggregators: List[ActorRef] =
    1.to(aggregatorCount).map(
      index => system.actorOf(DataAggregatorActor.props(analytics, extraIterations /
        aggregatorCount),
        s"DataAggregator$index")
    ).toList

  new InstanceGenerator(
      GenerationParams(aggregatorCount,
        extraIterations,
        groupMax,
        groupMin,
        random,
        analytics,
        dataAggregators,
        system)
    ).initIteration()
}
