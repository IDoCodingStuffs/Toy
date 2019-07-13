package net.codingstuffs.toy.engine.phenetics.calculators

import net.codingstuffs.toy.engine.agent.Agent.AgentParams
import net.codingstuffs.toy.engine.intake.parse.ConfigUtil
import net.codingstuffs.toy.engine.phenetics.AgentPheneticsGenerator

import scala.util.Random

object IterationBehavior {
  def getSelfUtility(mutation: String): Double =
    if (AgentPheneticsGenerator.GENE_SET.contains(mutation))
      AgentPheneticsGenerator.GENE_SET(mutation)
    else ConfigUtil.BASE_UTIL

  def pickMutatedSelfOrAttune(
    params: AgentParams,
    random: Random
  ): String = {

    val mutation = Mutations.mutate(params.phenome, new Random(random.nextLong))

    if (
      random.nextDouble() <= getSelfUtility(mutation._1) * params.groupWeights(params.turnInGroup) /
        params.groupWeights.values.sum)
      mutation._1
    else {
      val preferences = params.groupWeights.map(item =>
        params.groupWeights.values.filter(subitem => subitem == item._2).sum ->
          params.groupWeights.filter(subitem => subitem._2 == item._2).keySet
      )

      val normalizedPref = preferences.map(item => (item._1 / preferences.keySet.sum) -> item._2)
      val pickPossibilities = normalizedPref(probabilisticPick(normalizedPref.keySet.toList
        .sorted, random))

      val possibilities = pickPossibilities.toVector(random.nextInt(pickPossibilities.size))
      val pick = params.knownGroupPatterns(possibilities)

      //!TODO: Attunement happens at the mutated loc
      Mutations.attune(mutation, pick)
    }
  }

  //!TODO: Refactor?
  def probabilisticPick(myList: List[Double], random: Random): Double = {
    val roll = random.nextDouble()
    var sum = 0.0

    myList.indices.foreach(index => {
      sum += myList(index)
      if (roll < sum) {
        return myList(index)
      }
    }
    )
    0
  }
}
