package net.codingstuffs.abilene.simulation.decision_making.calculators

import net.codingstuffs.abilene.simulation.decision_making.models.AgentParamGenerator.DecisionParams
import net.codingstuffs.abilene.simulation.decision_making.models.DecisionMakingModel
import net.codingstuffs.abilene.simulation.decision_making.models.simplified.ArithmeticRoundup.{EgalitarianRoundup, SelfishRoundup, WeightedRoundup}
import net.codingstuffs.abilene.simulation.decision_making.models.simplified.Models.{SimpleDecisionVsCompromise, SocialImpactNSL, WeightedDecisionVsCompromise}

object ModelParamAdjuster {

  def adjust(implicit model:DecisionMakingModel, param: DecisionParams): DecisionParams = {
    val groupSize = param.groupWeights.keySet.size + 1

    model match {
      case SelfishRoundup =>
        DecisionParams(
          (param.selfParams._1, param.selfParams._2, 1),
          param.groupPreferences,
          param.groupWeights.map(weights => weights._1 -> 0.0)
        )

      case EgalitarianRoundup =>
        DecisionParams(
          (param.selfParams._1, param.selfParams._2, 1),
          param.groupPreferences,
          param.groupWeights.map(weights => weights._1 -> 1.0)
        )

      case WeightedRoundup =>
        DecisionParams(
          (param.selfParams._1, param.selfParams._2, param.selfParams._3),
          param.groupPreferences,
          param.groupWeights
        )

      case WeightedRoundup(sociotropy: Double, autonomy: Double) =>
        DecisionParams(
          (param.selfParams._1, param.selfParams._2, param.selfParams._3 * sociotropy),
          param.groupPreferences,
          param.groupWeights.map(weights => weights._1 -> weights._2 * autonomy / (groupSize + 1))
        )

      case SimpleDecisionVsCompromise(sociotropy, autonomy) =>
        DecisionParams(
          (param.selfParams._1, param.selfParams._2, 1.0),
          param.groupPreferences,
          param.groupWeights.map(weights => weights._1 -> 1.0)
        )

      case WeightedDecisionVsCompromise(sociotropy, autonomy) =>
        this normalize DecisionParams(
          (param.selfParams._1, param.selfParams._2, 1.0),
          param.groupPreferences,
          param.groupWeights.map(weights => weights._1 -> weights._2 * param.groupPreferences(weights._1))
        )

      case SocialImpactNSL => ???
    }
  }

  def normalize(param: DecisionParams): DecisionParams = {

    val factor = param.groupWeights.size / param.groupWeights.values.sum

    DecisionParams(
      (param.selfParams._1, param.selfParams._2, param.selfParams._3),
      param.groupPreferences,
      param.groupWeights.map(weights => weights._1 -> factor * weights._2)
    )
  }
}
