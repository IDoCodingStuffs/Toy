package net.codingstuffs.abilene.generators.random

import scala.util.Random

object FoldedGaussian {
  final val GAUSSIAN_GENERATOR = new FoldedGaussian
}

class FoldedGaussian extends Random {
  override def nextDouble: Double = math.abs(super.nextGaussian)

  def nextDouble(mean: Double): Double = math.abs(nextDouble + mean)
}
