package net.codingstuffs.abilene.model.decision_making.calculators

import net.codingstuffs.abilene.model.decision_making.models.Models._
import net.codingstuffs.abilene.model.decision_making.calculators.fuzzy.AgentFuzzifier
import net.codingstuffs.abilene.model.decision_making.generators.AgentParamGenerator.DecisionParams
import net.codingstuffs.abilene.model.decision_making.models.ArithmeticRoundup.{EgalitarianRoundup, SelfishRoundup, WeightedRoundup}

class DecisionCalculator(params: DecisionParams) {
  def get(implicit model: DecisionMakingModel): Boolean = {
    val groupMembers = params.groupWeights.keySet.toSeq

    val adjustedParams = ModelParamAdjuster.adjust(model, params)

    val self_val = adjustedParams.selfParams._2 * adjustedParams.selfParams._3

    val group_sum = groupMembers
      .map(member =>  adjustedParams.groupWeights(member) * adjustedParams.groupPreferences(member))
      .sum

    val group_val = group_sum / params.groupWeights.values.sum

    model match {
      case SelfishRoundup => params.selfParams._2 > 0.5

      case EgalitarianRoundup => (adjustedParams.selfParams._2 + adjustedParams.groupPreferences.values.sum) / (groupMembers.size + 1) > 0.5

      case WeightedRoundup => (self_val + group_sum) / (params.groupWeights.values.sum + adjustedParams.selfParams._3) > 0.5

      case WeightedRoundup(self: Double, group: Double) => {
        (self_val + group_sum) / (adjustedParams.groupWeights.values.sum + adjustedParams.selfParams._3) > 0.5
      }

      case SimpleSociotropyAutonomy(sociotropy, autonomy) =>
        val agentifiedGroup = DecisionParams(("group", group_val, 1), adjustedParams.groupPreferences, adjustedParams.groupWeights)

        val compromise = AgentFuzzifier.getIntersect(model.asInstanceOf[SimpleSociotropyAutonomy],
          (adjustedParams, agentifiedGroup))

        if (compromise.y > autonomy - compromise.y) compromise.x > 0.5 else self_val > 0.5

      case WeightedSociotropyAutonomy(sociotropy, autonomy) =>
        val agentifiedGroup = DecisionParams(("group", group_val, 1), adjustedParams.groupPreferences, adjustedParams.groupWeights)

        val compromise = AgentFuzzifier.getIntersect(model.asInstanceOf[WeightedSociotropyAutonomy],
          (adjustedParams, agentifiedGroup))

        if (compromise.y > autonomy - compromise.y) compromise.x > 0.5 else self_val > 0.5

      case FuzzyCentroid =>
        val sumAreas = groupMembers
          .map(member =>
            adjustedParams.groupWeights(member) * adjustedParams.groupPreferences(member) / 2)
          .sum + (adjustedParams.selfParams._2 * adjustedParams.selfParams._3 / 2)


        true
    }
  }
}
