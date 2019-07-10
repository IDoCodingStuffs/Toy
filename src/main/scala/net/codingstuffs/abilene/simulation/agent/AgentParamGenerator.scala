package net.codingstuffs.abilene.simulation.agent


import net.codingstuffs.abilene.simulation.agent.AgentParamGenerator.DecisionParams

import scala.util.Random

object AgentParamGenerator {

  final case class DecisionParams(selfParams: (Int, Double, Double),
    groupPreferences: Map[Int, String],
    groupWeights: Map[Int, Double])

}

class AgentParamGenerator(studyModel: AgentBehaviorModel,
  randomGenerators                  : (Random, Random),
  memberIndices                     : Set[Int]) {

  implicit var self: String = _

  val preferenceGenerator: Random = randomGenerators._1
  val weightsGenerator: Random = randomGenerators._2


  def getSelfParams(name: String): (Int, Double, Double) = (self.toInt, preferenceGenerator
    .nextDouble(), weightsGenerator.nextDouble())

  //!TODO: Retrieve em
  def groupExpressions(implicit groupMembers: Set[Int]): Map[Int, String] =
    groupMembers.filter(member => member != self.toInt).map(member => member -> "AAAA").toMap

  def groupWeights(implicit groupMembers: Set[Int],
    max_deviation                       : Int = 3): Map[Int, Double] =
    groupMembers.filter(member => member != self.toInt).map(member => member -> weightsGenerator
      .nextDouble).toMap


  def get: DecisionParams = DecisionParams(getSelfParams(self), groupExpressions(memberIndices),
    groupWeights(memberIndices))
}
