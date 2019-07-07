package net.codingstuffs.abilene.simulation.decision_making.models


import net.codingstuffs.abilene.simulation.decision_making.models.AgentParamGenerator.DecisionParams

import scala.util.Random

object AgentParamGenerator {

  final case class DecisionParams(selfParams: (String, Double, Double),
    groupPreferences                        : Map[String, Double],
    groupWeights                            : Map[String, Double])

}

class AgentParamGenerator(studyModel: AgentBehaviorModel, randomGenerators: (Random, Random)) {

  implicit var self: String = _
  implicit var memberNames: Set[String] = _

  val preferenceGenerator: Random = randomGenerators._1
  val weightsGenerator: Random = randomGenerators._2


  def getSelfParams(name: String): (String, Double, Double) = (self, preferenceGenerator
    .nextDouble(), weightsGenerator.nextDouble())


  def groupPreferences(implicit groupMembers: Set[String]): Map[String, Double] =
    groupMembers.filter(member => member != self).map(member => member -> preferenceGenerator
      .nextDouble).toMap

  def groupWeights(implicit groupMembers: Set[String],
    max_deviation                       : Int = 3): Map[String, Double] =
    groupMembers.filter(member => member != self).map(member => member -> weightsGenerator
      .nextDouble).toMap

  def get: DecisionParams = DecisionParams(getSelfParams(self), groupPreferences, groupWeights)
}
