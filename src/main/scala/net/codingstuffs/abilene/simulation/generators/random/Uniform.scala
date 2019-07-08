package net.codingstuffs.abilene.simulation.generators.random

import scala.util.Random

object Uniform {
  final val GENERATOR = new Uniform
}

class Uniform extends Random {
  override def nextDouble: Double = super.nextDouble
}
