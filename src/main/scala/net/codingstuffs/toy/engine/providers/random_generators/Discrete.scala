package net.codingstuffs.toy.engine.providers.random_generators

import net.codingstuffs.toy.engine.intake.parse.ConfigUtil

import scala.util.Random

object Discrete {
  final def GENERATOR(values: Seq[Double]) = new Discrete(values)
}

class Discrete(values: Seq[Double]) extends Random{
  self.setSeed(ConfigUtil.MAIN_GENERATOR_SEED)

  override def nextDouble: Double = shuffle(values).head
}
