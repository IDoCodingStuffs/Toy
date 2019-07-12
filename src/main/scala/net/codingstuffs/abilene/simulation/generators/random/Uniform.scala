package net.codingstuffs.abilene.simulation.generators.random

import net.codingstuffs.abilene.intake.parse.ConfigUtil

import scala.util.Random

object Uniform {
  final val GENERATOR = new Uniform
}

class Uniform extends Random {
  self.setSeed(ConfigUtil.MAIN_GENERATOR_SEED)

  override def nextDouble: Double = super.nextDouble
}
