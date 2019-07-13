package net.codingstuffs.toy.engine.providers.random_generators

import net.codingstuffs.toy.engine.intake.parse.ConfigUtil

import scala.util.Random

object Uniform {
  final val GENERATOR = new Uniform
}

class Uniform extends Random {
  self.setSeed(ConfigUtil.MAIN_GENERATOR_SEED)

  override def nextDouble: Double = super.nextDouble
}
