package net.codingstuffs.abilene.model.decision_making.generators.random

import scala.util.Random

object Static {
  final val GENERATOR = new Static(0, 0.25, 0.5, 0.75, 1)
}

class Static(values: Double*) extends Random{
  override def nextDouble: Double = shuffle(values).head
}
