package net.codingstuffs.abilene.simulation.environment

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Random

object AgentWorld {
  private val config = ConfigFactory.load()
  private val random = Random

  final val FACTOR_COUNT = config.getInt("environment.factor.count")
  final val FACTOR_LENGTH = config.getInt("environment.factor.length")

  //!TODO: Move this to some pattern matcher outside this class to utilize the Models obj
  private final val FACTOR_TYPE = config.getString("environment.factor.type")

  def get: Set[String] = FACTOR_TYPE match {
    case "Static" => 1.to(FACTOR_COUNT).map(_ => random.nextString(FACTOR_LENGTH)).toSet
  }
}

