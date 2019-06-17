package net.codingstuffs.abilene.model.decision_making.calculators

import net.codingstuffs.abilene.model.decision_making.Models.DecisionMakingModel
import net.codingstuffs.abilene.model.decision_making.generators.AgentParamGenerator.DecisionParams

object DecisionCalculator {
  def get(implicit model: DecisionMakingModel, params: DecisionParams): Boolean = {
    val groupMembers = params.groupWeights.keySet

    val adjustedParams = ModelParamAdjuster.adjust
    val self_val = adjustedParams.selfParams._2 * adjustedParams.selfParams._3
    val group_val = groupMembers
      .map(member =>
        adjustedParams.groupWeights(member) * adjustedParams.groupPreferences(member))
      .sum

    self_val + (group_val/groupMembers.size) > 1
  }
}
