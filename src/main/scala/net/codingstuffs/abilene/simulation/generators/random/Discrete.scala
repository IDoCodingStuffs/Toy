package net.codingstuffs.abilene.simulation.generators.random

import scala.util.Random

object Discrete {
  final def GENERATOR(values: Seq[Double]) = new Discrete(values)
}

class Discrete(values: Seq[Double]) extends Random{
  override def nextDouble: Double = shuffle(values).head
}
