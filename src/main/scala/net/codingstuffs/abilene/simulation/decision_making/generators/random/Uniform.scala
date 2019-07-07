package net.codingstuffs.abilene.simulation.decision_making.generators.random

import scala.util.Random

object Uniform {
  final val GENERATOR = new Uniform
}

class Uniform extends Random {
  override def nextDouble: Double = super.nextDouble
}
