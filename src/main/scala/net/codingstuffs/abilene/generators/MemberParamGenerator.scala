package net.codingstuffs.abilene.generators

import net.codingstuffs.abilene.model.Member.MemberParams
import net.codingstuffs.abilene.model.logic.DecisionMakingModels.{LogicModel, Selfish}

import scala.util.Random

object MemberParamGenerator {

  val decisionModel: LogicModel = Selfish
  val random = new Random()

  def memberPreferences(groupMembers: Set[String]): Map[String, Double] =
    groupMembers.map(member => member -> random.nextInt(101) / 100.0).toMap

  def memberWeights(groupMembers: Set[String], max_deviation: Int = 3): Map[String, Double] =
    groupMembers.map(member => member -> random.nextInt(max_deviation * 100 + 1) / 100.0).toMap

  def gaussianMemberWeights(groupMembers: Set[String], mean: Double = 0.5): Map[String, Double] =
    normalizeWeights(groupMembers.map(member => member -> (math.abs(random.nextGaussian()) + mean)).toMap)

  def normalizeWeights(weights: Map[String, Double]): Map[String, Double] =
    weights.map(weight => weight._1 -> weight._2 * (weights.keySet.size / weights.values.sum))

  //!TODO: Change passed tuple for pain-avoidance vs reward once calculation changed
  def generate(groupMembers: Set[String]): MemberParams = MemberParams((0,0), decisionModel, memberWeights(groupMembers), memberPreferences(groupMembers))
}
