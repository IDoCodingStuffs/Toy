package net.codingstuffs.toy.engine.providers.random_generators

import net.codingstuffs.toy.engine.intake.parse.ConfigUtil
import org.apache.commons.math3.distribution.BetaDistribution

import scala.util.Random

object Beta {
  def GENERATOR(alpha: Double, beta: Double) = new Beta(alpha, beta)
}

class Beta(alpha: Double, beta: Double) extends Random {
  self.setSeed(ConfigUtil.MAIN_GENERATOR_SEED)

  override def nextDouble: Double =
    new BetaDistribution(alpha, beta).inverseCumulativeProbability(super.nextDouble)
}