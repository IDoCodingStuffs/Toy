package net.codingstuffs.abilene.model.decision_making.calculators

import net.codingstuffs.abilene.model.decision_making.Models.{DecisionMakingModel, SimpleRoundup, SocialImpactNSL, SociotropyAutonomy}
import net.codingstuffs.abilene.model.decision_making.generators.AgentParamGenerator.DecisionParams

object ModelParamAdjuster {

  def adjust(model:DecisionMakingModel, param: DecisionParams): DecisionParams = {
    model match {
      case SimpleRoundup => ???
      case SociotropyAutonomy => ???
      case SocialImpactNSL => ???
    }
  }
}
