package net.codingstuffs.abilene.model.logic

object DecisionMakingModels {

  sealed abstract class LogicModel
  sealed abstract class DecisionMakingModel

  final case object Selfish extends LogicModel

  final case object SimpleConsensusSeeking extends LogicModel

  final case object WeightedConsensusSeeking extends LogicModel


  final case object SimpleRoundup extends DecisionMakingModel

  final case object RandomBellCurveRoundup extends DecisionMakingModel

  //Bieling, Beck, Brown (2000)
  final case object SociotropyAutonomy extends DecisionMakingModel

  //Nowak, Szamrej, Latané (1990), credit to F. Kalvas for advice
  final case object SocialImpactNSL extends DecisionMakingModel

}
