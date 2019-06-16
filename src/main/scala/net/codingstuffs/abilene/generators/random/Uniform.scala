package net.codingstuffs.abilene.generators.random

import scala.util.Random

object Uniform {
  final val UNIFORM_GENERATOR = new Uniform
}

class Uniform extends Random{
  override def nextDouble: Double = super.nextDouble
}
