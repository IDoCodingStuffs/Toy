package net.codingstuffs.abilene.generators.random

import scala.util.Random

object Static {
  final val STATIC_GENERATOR = new Static
}

class Static extends Random{
  override def nextDouble: Double = 0.5
}
