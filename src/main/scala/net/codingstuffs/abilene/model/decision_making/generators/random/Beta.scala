package net.codingstuffs.abilene.model.decision_making.generators.random

import org.apache.commons.math3.distribution.BetaDistribution

import scala.util.Random

object Beta {
  final val GENERATOR = new Beta
}

class Beta extends Random {
  override def nextDouble(): Double =
    new BetaDistribution(2, 2).inverseCumulativeProbability(super.nextDouble)

  def nextDouble(alpha: Double, beta: Double): Double =
    new BetaDistribution(alpha, beta).inverseCumulativeProbability(super.nextDouble)
}