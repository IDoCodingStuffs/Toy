package net.codingstuffs.abilene.simulation.decision_making.models.simplified

import net.codingstuffs.abilene.simulation.decision_making.models.DecisionMakingModel


object Models {

 //Naomi E. Leonard ; Tian Shen ; Benjamin Nabet ; Luca Scardovi ; Iain D. Couzin ; Simon A. Levin
 //Proceedings of the National Academy of Sciences, 03 January 2012, Vol.109(1), p.227
 final case class SimpleDecisionVsCompromise(decision: Double, compromise: Double) extends DecisionMakingModel

 final case class WeightedDecisionVsCompromise(decision: Double, compromise: Double) extends DecisionMakingModel

 //Nowak, Szamrej, Latané (1990), credit to F. Kalvas for advice
 final case object SocialImpactNSL extends DecisionMakingModel

 final case object FuzzyCentroid extends DecisionMakingModel

 //Baronchelli (2018)
 //Moran process
}