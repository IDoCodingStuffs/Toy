package net.codingstuffs.abilene.model.decision_making.generators.random

import scala.util.Random

object Static {
  final val GENERATOR = new Static
}

class Static extends Random{
  override def nextDouble: Double = 0.5
}
