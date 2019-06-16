package net.codingstuffs.abilene.model.decision_making.calculators

import net.codingstuffs.abilene.model.decision_making.Models.DecisionMakingModel
import net.codingstuffs.abilene.model.decision_making.generators.AgentParamGenerator.DecisionParams

object DecisionCalculator {
  def get(implicit model: DecisionMakingModel, params: DecisionParams): Boolean = {
    val self_val = params.selfParams._2 * params.selfParams._3

    val group_val = params.groupWeights.keySet.map(
      member => params.groupWeights(member) * params.groupPreferences(member)
    ).sum

    self_val > 0.5
  }
}
