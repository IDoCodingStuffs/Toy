package net.codingstuffs.abilene.model.decision_making.calculators

import net.codingstuffs.abilene.model.decision_making.Models.{DecisionMakingModel, NaiveRoundup, SimpleSociotropyAutonomy}
import net.codingstuffs.abilene.model.decision_making.calculators.fuzzy.AgentFuzzifier
import net.codingstuffs.abilene.model.decision_making.generators.AgentParamGenerator.DecisionParams

object DecisionCalculator {
  def get(implicit model: DecisionMakingModel, params: DecisionParams): Boolean = {
    val groupMembers = params.groupWeights.keySet

    val adjustedParams = ModelParamAdjuster.adjust

    val self_val = adjustedParams.selfParams._2 * adjustedParams.selfParams._3

    val group_val = groupMembers
      .map(member =>
        adjustedParams.groupWeights(member) * adjustedParams.groupPreferences(member))
      .sum / groupMembers.size

    model match {
      case NaiveRoundup => self_val > 0.5
      case SimpleSociotropyAutonomy(sociotropy, autonomy) => {
        val agentifiedGroup = DecisionParams(("group", group_val, 1), params.groupPreferences, params.groupWeights)

        val compromise = AgentFuzzifier.getIntersect(model.asInstanceOf[SimpleSociotropyAutonomy],
          (params, agentifiedGroup))

        if (compromise.y > autonomy / 2) compromise.x > 0.5 else self_val > 0.5
      }
    }
  }
}
