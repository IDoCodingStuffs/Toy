package net.codingstuffs.abilene.simulation.decision_making.generators.random

import scala.util.Random

object FoldedGaussian {
  final def GENERATOR(mean: Double) = new FoldedGaussian(mean)
}

class FoldedGaussian(mean: Double) extends Random {
  override def nextDouble: Double = math.abs(super.nextGaussian) + mean
}
