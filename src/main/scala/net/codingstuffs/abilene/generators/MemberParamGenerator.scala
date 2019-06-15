package net.codingstuffs.abilene.generators

import net.codingstuffs.abilene.model.Member.MemberParams
import net.codingstuffs.abilene.model.logic.DecisionMakingModels.{LogicModel, Selfish, SimpleConsensusSeeking, WeightedConsensusSeeking}
import org.apache.commons.math3.distribution.BetaDistribution
import org.apache.commons.math3.random.RandomGenerator

import scala.util.Random

object MemberParamGenerator {

  val decisionModel: LogicModel = SimpleConsensusSeeking
  val random = new Random()

  def memberPreferences(groupMembers: Set[String]): Map[String, Double] =
    groupMembers.map(member => member -> random.nextInt(101) / 100.0).toMap

  def betaThreshold(implicit alpha: Double = 2, beta: Double = 2): Double = new BetaDistribution(alpha, beta).inverseCumulativeProbability(random.nextDouble)

  def memberWeights(groupMembers: Set[String], max_deviation: Int = 3): Map[String, Double] =
    groupMembers.map(member => member -> random.nextInt(max_deviation * 100 + 1) / 100.0).toMap

  def gaussianMemberWeights(groupMembers: Set[String], mean: Double = 0.5): Map[String, Double] =
    normalizeWeights(groupMembers.map(member => member -> (math.abs(random.nextGaussian()) + mean)).toMap)

  def normalizeWeights(weights: Map[String, Double]): Map[String, Double] =
    weights.map(weight => weight._1 -> weight._2 * (weights.keySet.size / weights.values.sum))

  def generate(groupMembers: Set[String]): MemberParams = MemberParams(betaThreshold, decisionModel, memberWeights(groupMembers), memberPreferences(groupMembers))
}
