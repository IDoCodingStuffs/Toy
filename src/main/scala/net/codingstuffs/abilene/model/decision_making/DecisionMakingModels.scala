package net.codingstuffs.abilene.model.decision_making

object DecisionMakingModels {

  sealed abstract class DecisionMakingModel

  final case object SimpleRoundup extends DecisionMakingModel

  //Bieling, Beck, Brown (2000)
  final case class SociotropyAutonomy(sociotropy: Double, autonomy: Double) extends DecisionMakingModel

  //Nowak, Szamrej, Latané (1990), credit to F. Kalvas for advice
  final case object SocialImpactNSL extends DecisionMakingModel

}
