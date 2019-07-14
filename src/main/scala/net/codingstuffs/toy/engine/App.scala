package net.codingstuffs.toy.engine

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import net.codingstuffs.toy.engine.analytics.AnalyticsGenerationActor
import net.codingstuffs.toy.engine.intake.parse.ConfigUtil
import net.codingstuffs.toy.engine.iteration.{DataAggregatorActor, IterationInstantiator}
import net.codingstuffs.toy.engine.providers.InstanceGenerator
import net.codingstuffs.toy.engine.providers.InstanceGenerator.GenerationParams

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object App extends App {

  val system = ActorSystem("Abilene")

  private val random = new Random(ConfigUtil.MAIN_GENERATOR_SEED)
  private implicit val timeout: Timeout = Timeout(FiniteDuration.apply(5, "seconds"))

  val analytics = system.actorOf(AnalyticsGenerationActor.props, "AnalyticsGenerator")
  val ticker = system.actorOf(IterationInstantiator.props(system), "IterationManager")

  val dataAggregators: List[ActorRef] = 1.to(ConfigUtil.AGGREGATOR_COUNT)
    .map(index =>
      system.actorOf(DataAggregatorActor.props(analytics, ticker, ConfigUtil.EXTRA_ITERATIONS / ConfigUtil.AGGREGATOR_COUNT),
        name = s"DataAggregator$index")
    ).toList

  new InstanceGenerator(
      GenerationParams(
        random,
        analytics,
        dataAggregators,
        system)
    ).initIteration()
}
