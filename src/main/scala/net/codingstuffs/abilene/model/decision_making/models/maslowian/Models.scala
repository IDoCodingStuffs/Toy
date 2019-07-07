package net.codingstuffs.abilene.model.decision_making.models.maslowian

import net.codingstuffs.abilene.model.decision_making.models.DecisionMakingModel

object Models {

  final case class KNSUpdated(params: Map[String, Double]) extends DecisionMakingModel

}