package net.codingstuffs.abilene.model.decision_making.generators

import net.codingstuffs.abilene.model.decision_making.generators.AgentParamGenerator.DecisionParams
import net.codingstuffs.abilene.model.decision_making.generators.random.{Beta, FoldedGaussian, Static, Uniform}

import scala.util.Random

object AgentParamGenerator {

  final case class DecisionParams(selfParams: (String, Double, Double), groupPreferences: Map[String, Double], groupWeights: Map[String, Double])

}

class AgentParamGenerator(random: Random) {

  implicit var self: String = _
  implicit var memberNames: Set[String] = _

  def getSelfParams(name: String): (String, Double, Double) = (self, random.nextDouble(), random.nextDouble())

  def groupPreferences(implicit groupMembers: Set[String]): Map[String, Double] =
    groupMembers.filter(member => member != self).map(member => member -> random.nextDouble).toMap

  def groupWeights(implicit groupMembers: Set[String], max_deviation: Int = 3): Map[String, Double] =
    groupMembers.filter(member => member != self).map(member => member -> random.nextDouble).toMap

  def normalizeWeights(weights: Map[String, Double]): Map[String, Double] =
    weights.map(weight => weight._1 -> weight._2 * (weights.keySet.size / weights.values.sum))

  def get: DecisionParams = DecisionParams(getSelfParams(self), groupPreferences, groupWeights)
}
