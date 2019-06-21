package net.codingstuffs.abilene.model.decision_making.generators.random

import scala.util.Random

object Discrete {
  final val GENERATOR = new Discrete(0, 0.25, 0.5, 0.75, 1)
}

class Discrete(values: Double*) extends Random{
  override def nextDouble: Double = shuffle(values).head
}
