package net.codingstuffs.abilene.model.decision_making.calculators

import net.codingstuffs.abilene.model.decision_making.Models.{DecisionMakingModel, SimpleRoundup, SocialImpactNSL, SociotropyAutonomy}
import net.codingstuffs.abilene.model.decision_making.generators.AgentParamGenerator.DecisionParams

object ModelParamAdjuster {

  def adjust(model:DecisionMakingModel, param: DecisionParams): DecisionParams = {
    model match {
      case SimpleRoundup =>
        DecisionParams(
          (param.selfParams._1, param.selfParams._2, 1),
          param.groupPreferences,
          param.groupWeights.map(weights => weights._1 -> 0.0)
        )

      case SociotropyAutonomy => ???
      case SocialImpactNSL => ???
    }
  }
}
