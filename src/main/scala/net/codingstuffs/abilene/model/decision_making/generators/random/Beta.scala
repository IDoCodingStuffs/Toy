package net.codingstuffs.abilene.model.decision_making.generators.random

import org.apache.commons.math3.distribution.BetaDistribution

import scala.util.Random

object Beta {
  def GENERATOR(alpha: Double, beta: Double) = new Beta(alpha, beta)
}

class Beta(alpha: Double, beta: Double) extends Random {
   override def nextDouble: Double =
    new BetaDistribution(alpha, beta).inverseCumulativeProbability(super.nextDouble)
}