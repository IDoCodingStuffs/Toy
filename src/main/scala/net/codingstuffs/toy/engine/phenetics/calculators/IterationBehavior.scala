package net.codingstuffs.toy.engine.phenetics.calculators

import net.codingstuffs.toy.engine.providers.AgentParamGenerator.ExpressionParams

import scala.util.Random

object IterationBehavior {
  def pickMutatedSelfOrAttune(
    mutatedPhenome: (String, Int),
    initialPhenome: String,
    params: ExpressionParams,
    random                                  : Random
  ): String = {
    if (
      random.nextDouble() <= params.selfParams._3 / params.groupWeights.values.sum
    ) mutatedPhenome._1
    else {
      val preferences = params.groupWeights.map(item =>
        params.groupWeights.values.filter(subitem => subitem == item._2).sum ->
          params.groupWeights.filter(subitem => subitem._2 == item._2).keySet
      )

      val normalizedPref = preferences.map(item => (item._1 / preferences.keySet.sum) -> item._2)
      val pickPossibilities = normalizedPref(probabilisticPick(normalizedPref.keySet.toList
        .sorted, random))

      val pick = params.groupExpressions(
        pickPossibilities.toVector(random.nextInt(pickPossibilities.size)))

      //!TODO: Attunement happens at the mutated loc
      Mutations.attune(mutatedPhenome, pick)
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
