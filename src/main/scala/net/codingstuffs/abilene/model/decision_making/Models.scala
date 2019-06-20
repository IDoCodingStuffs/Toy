package net.codingstuffs.abilene.model.decision_making

object Models {

  sealed abstract class DecisionMakingModel

  final case object SelfishRoundup extends DecisionMakingModel
  final case object EgalitarianRoundup extends DecisionMakingModel
  final case object WeightedRoundup extends DecisionMakingModel


  final case object FuzzyCentroid extends DecisionMakingModel

  //Bieling, Beck, Brown (2000)
  final case class SimpleSociotropyAutonomy(sociotropy: Double, autonomy: Double) extends DecisionMakingModel
  final case class WeightedSociotropyAutonomy(sociotropy: Double, autonomy: Double) extends DecisionMakingModel

  //Nowak, Szamrej, Latané (1990), credit to F. Kalvas for advice
  final case object SocialImpactNSL extends DecisionMakingModel

}
