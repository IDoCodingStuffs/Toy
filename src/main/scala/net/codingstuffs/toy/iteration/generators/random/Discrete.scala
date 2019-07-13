package net.codingstuffs.toy.iteration.generators.random

import net.codingstuffs.toy.intake.parse.ConfigUtil

import scala.util.Random

object Discrete {
  final def GENERATOR(values: Seq[Double]) = new Discrete(values)
}

class Discrete(values: Seq[Double]) extends Random{
  self.setSeed(ConfigUtil.MAIN_GENERATOR_SEED)

  override def nextDouble: Double = shuffle(values).head
}
