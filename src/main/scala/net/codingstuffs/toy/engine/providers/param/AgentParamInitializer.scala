package net.codingstuffs.toy.engine.providers.param

import net.codingstuffs.toy.engine.agent.Agent.AgentParams
import net.codingstuffs.toy.engine.phenetics.AgentPheneticsGenerator

import scala.util.Random

object AgentParamInitializer {
  var groupPhenomes: Map[String, Map[Int, String]] = Map()
}

class AgentParamInitializer(
  phenome: String,
  groupId: String,
  turnInGroup: Int,
  groupMembers: Set[Int],
  randomSeed: Long) {

  import AgentParamInitializer._

  private val random = new Random(randomSeed)

  val groupPhenome: Map[Int, String] = {
    if (!groupPhenomes.keySet.contains(groupId))
      groupPhenomes +=
        groupId -> groupMembers.map(index => index -> AgentPheneticsGenerator.get).toMap
    groupPhenomes(groupId)
  }

  def groupExpressions(groupMembers: Set[Int]): Map[Int, String] =
    groupMembers //.filter(member => member != turnInGroup)
      .map(member => member -> groupPhenome(member)).toMap

  //!TODO: Review logic here
  def groupWeights: Map[Int, Double] =
    groupMembers
      .map(member => member -> new Random(random.nextLong).nextDouble()).toMap

  val adjustedParams: AgentParams = {
    AgentParams(
      phenome,
      groupId,
      turnInGroup,
      groupMembers,
      //!TODO: Mathod for this instead?
      groupWeights
        .filter(item => item._1 != turnInGroup)
        .map(item => item._1 -> (1 / MaslowianParamGenerator.INSTANCE.getMaslowianSum) * item._2) +
        (turnInGroup -> groupWeights(turnInGroup)),
      groupExpressions(groupMembers),
      MaslowianParamGenerator.INSTANCE.getParams
    )
  }
}
