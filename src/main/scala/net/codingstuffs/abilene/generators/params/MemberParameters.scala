package net.codingstuffs.abilene.generators.params

import net.codingstuffs.abilene.model.Member.MemberParams
import net.codingstuffs.abilene.model.decision_making.DecisionMakingModels.{DecisionMakingModel, SimpleRoundup}

import scala.util.Random

class MemberParameters(decisionModel: DecisionMakingModel, random: Random) {

  def memberPreferences(groupMembers: Set[String]): Map[String, Double] =
    groupMembers.map(member => member -> random.nextInt(101) / 100.0).toMap

  def memberWeights(groupMembers: Set[String], max_deviation: Int = 3): Map[String, Double] =
    groupMembers.map(member => member -> random.nextInt(max_deviation * 100 + 1) / 100.0).toMap

  def normalizeWeights(weights: Map[String, Double]): Map[String, Double] =
    weights.map(weight => weight._1 -> weight._2 * (weights.keySet.size / weights.values.sum))

  def generate(groupMembers: Set[String]): MemberParams =
    MemberParams(
      random.nextDouble,
      memberWeights(groupMembers),
      memberPreferences(groupMembers)
    )
}
