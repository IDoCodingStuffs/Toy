package net.codingstuffs.toy.engine.providers.random_generators

import net.codingstuffs.toy.engine.intake.parse.ConfigUtil

import scala.util.Random

object FoldedGaussian {
  final def GENERATOR(mean: Double) = new FoldedGaussian(mean, 1)
  final def GENERATOR(mean: Double, sd: Double) = new FoldedGaussian(mean, sd)
}

class FoldedGaussian(mean: Double, sd: Double) extends Random {
  self.setSeed(ConfigUtil.MAIN_GENERATOR_SEED)

  override def nextDouble: Double = math.abs(super.nextGaussian * sd) + mean
}
