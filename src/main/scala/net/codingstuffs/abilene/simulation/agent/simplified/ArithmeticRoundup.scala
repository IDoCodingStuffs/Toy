package net.codingstuffs.abilene.simulation.agent.simplified

import net.codingstuffs.abilene.simulation.agent.DecisionMakingModel

object ArithmeticRoundup {
  final case object SelfishRoundup extends DecisionMakingModel
  final case object EgalitarianRoundup extends DecisionMakingModel
  final case object WeightedRoundup extends DecisionMakingModel

  //Bieling, Beck, Brown (2000)
  final case class WeightedRoundup(autonomy: Double, sociotropy: Double) extends DecisionMakingModel

}
