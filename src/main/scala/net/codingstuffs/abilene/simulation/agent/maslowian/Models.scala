package net.codingstuffs.abilene.simulation.agent.maslowian

import net.codingstuffs.abilene.simulation.agent.DecisionMakingModel

object Models {

  final case class KNSUpdated(params: Map[String, Double]) extends DecisionMakingModel

}