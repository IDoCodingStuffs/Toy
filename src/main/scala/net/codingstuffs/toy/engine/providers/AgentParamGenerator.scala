package net.codingstuffs.toy.engine.providers

import net.codingstuffs.toy.engine.phenetics.AgentPheneticsGenerator

import scala.util.Random

object AgentParamGenerator {

  final case class ExpressionParams(selfParams: (Int, String, Double),
    groupExpressions: Map[Int, String],
    groupWeights: Map[Int, Double])

  var groupPhenomes: Map[String, Map[Int, String]] = Map()

}

class AgentParamGenerator(
  randomGenerators: (Random, Random),
  memberIndices: Set[Int],
  groupId                           : String) {

  import AgentParamGenerator._

  implicit var self: String = _

  val preferenceGenerator: Random = randomGenerators._1
  val weightsGenerator: Random = randomGenerators._2

  val agentGenes: Map[Int, String ] =
    if (groupPhenomes.keySet.contains(groupId)) groupPhenomes(groupId)
    else memberIndices.map(index => index -> AgentPheneticsGenerator.get).toMap


  def getSelfParams(name: String): (Int, String, Double) = (self.toInt, agentGenes(self.toInt),
    weightsGenerator.nextDouble())

  def groupExpressions(implicit groupMembers: Set[Int]): Map[Int, String] =
    groupMembers.filter(member => member != self.toInt)
      .map(member => member -> agentGenes(member)).toMap

  def groupWeights(implicit groupMembers: Set[Int],
    max_deviation                       : Int = 3): Map[Int, Double] =
    groupMembers.filter(member => member != self.toInt).map(member => member -> weightsGenerator
      .nextDouble).toMap


  def get: ExpressionParams = ExpressionParams(getSelfParams(self), groupExpressions(memberIndices),
    groupWeights(memberIndices))
}
