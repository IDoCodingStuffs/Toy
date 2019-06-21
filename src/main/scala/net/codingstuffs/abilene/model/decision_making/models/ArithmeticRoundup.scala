package net.codingstuffs.abilene.model.decision_making.models

import net.codingstuffs.abilene.model.decision_making.models.Models.DecisionMakingModel

object ArithmeticRoundup {
  final case object SelfishRoundup extends DecisionMakingModel
  final case object EgalitarianRoundup extends DecisionMakingModel
  final case object WeightedRoundup extends DecisionMakingModel

  //Bieling, Beck, Brown (2000)
  final case class WeightedRoundup(autonomy: Double, sociotropy: Double) extends DecisionMakingModel

}
