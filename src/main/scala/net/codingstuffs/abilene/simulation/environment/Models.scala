package net.codingstuffs.abilene.simulation.environment

object Models {
  final case class StaticNodes(items: Set[String])
  final case class DynamicNodes(items: Map[String, Double])
}
