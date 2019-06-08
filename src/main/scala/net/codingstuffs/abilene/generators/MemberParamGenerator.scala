package net.codingstuffs.abilene.generators

import net.codingstuffs.abilene.model.Member.MemberParams

import scala.util.Random

object MemberParamGenerator {

  val random = new Random()

  def memberPreferences(groupMembers: Set[String]): Map[String, Double] =
    groupMembers.map(member => member -> random.nextInt(101) / 100.0).toMap

  def gaussianMemberWeights(groupMembers: Set[String], mean: Double = 0.5): Map[String, Double] =
    normalizeWeights(groupMembers.map(member => member -> (math.abs(random.nextGaussian()) + mean)).toMap)

  def normalizeWeights(weights: Map[String, Double]): Map[String, Double] =
    weights.map(weight => weight._1 -> weight._2 * (weights.keySet.size / weights.values.sum))

  def generate(groupMembers: Set[String]): MemberParams = MemberParams(gaussianMemberWeights(groupMembers), memberPreferences(groupMembers))
}
