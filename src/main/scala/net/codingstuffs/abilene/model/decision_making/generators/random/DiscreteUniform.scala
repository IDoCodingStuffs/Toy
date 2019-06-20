package net.codingstuffs.abilene.model.decision_making.generators.random

object DiscreteUniform {
  final val GENERATOR = new DiscreteUniform(0, 0.25, 0.5, 0.75, 1)
}

class DiscreteUniform(values: Double*) extends Uniform {
  override def nextDouble: Double = shuffle(values).head
}
