package net.codingstuffs.abilene.model.decision_making.models

import net.codingstuffs.abilene.model.decision_making.models.Models.DecisionMakingModel

object ArithmeticRoundup {
  final case object SelfishRoundup extends DecisionMakingModel
  final case object EgalitarianRoundup extends DecisionMakingModel
  final case object WeightedRoundup extends DecisionMakingModel
  final case class WeightedRoundup(self: Double, group: Double) extends DecisionMakingModel

}
