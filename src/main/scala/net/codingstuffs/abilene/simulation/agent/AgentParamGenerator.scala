package net.codingstuffs.abilene.simulation.agent


import net.codingstuffs.abilene.simulation.agent.AgentParamGenerator.ExpressionParams
import net.codingstuffs.abilene.simulation.agent.genetics.AgentGeneticsGenerator

import scala.util.Random

object AgentParamGenerator {

  final case class ExpressionParams(selfParams: (Int, String, Double),
    groupExpressions: Map[Int, String],
    groupWeights: Map[Int, Double])

  var groupGenomes: Map[String, Map[Int, String]] = Map()

}

class AgentParamGenerator(studyModel: AgentBehaviorModel,
  randomGenerators: (Random, Random),
  memberIndices: Set[Int],
  groupId                           : String) {

  import AgentParamGenerator._

  implicit var self: String = _

  val preferenceGenerator: Random = randomGenerators._1
  val weightsGenerator: Random = randomGenerators._2

  private val agentGenes: Map[Int, String ] =
    if (groupGenomes.keySet.contains(groupId)) groupGenomes(groupId)
    else memberIndices.map(index => index -> AgentGeneticsGenerator.get).toMap


  def getSelfParams(name: String): (Int, String, Double) = (self.toInt, agentGenes(self.toInt),
    weightsGenerator.nextDouble())

  //!TODO: Retrieve em
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
